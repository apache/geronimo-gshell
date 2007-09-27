/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.gshell.remote.server;

import java.net.URI;
import java.security.PublicKey;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.geronimo.gshell.DefaultEnvironment;
import org.apache.geronimo.gshell.common.Duration;
import org.apache.geronimo.gshell.common.NamedThreadFactory;
import org.apache.geronimo.gshell.common.Notification;
import org.apache.geronimo.gshell.common.tostring.ToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.lookup.EnvironmentLookup;
import org.apache.geronimo.gshell.lookup.IOLookup;
import org.apache.geronimo.gshell.remote.RemoteShell;
import org.apache.geronimo.gshell.remote.crypto.CryptoContext;
import org.apache.geronimo.gshell.remote.message.CloseShellMessage;
import org.apache.geronimo.gshell.remote.message.ConnectMessage;
import org.apache.geronimo.gshell.remote.message.EchoMessage;
import org.apache.geronimo.gshell.remote.message.ExecuteMessage;
import org.apache.geronimo.gshell.remote.message.LoginMessage;
import org.apache.geronimo.gshell.remote.message.OpenShellMessage;
import org.apache.geronimo.gshell.remote.message.RshMessage;
import org.apache.geronimo.gshell.shell.Environment;
import org.apache.geronimo.gshell.whisper.message.MessageHandler;
import org.apache.geronimo.gshell.whisper.message.MessageHandlerSupport;
import org.apache.geronimo.gshell.whisper.session.SessionAttributeBinder;
import org.apache.geronimo.gshell.whisper.transport.TransportFactory;
import org.apache.geronimo.gshell.whisper.transport.TransportFactoryLocator;
import org.apache.geronimo.gshell.whisper.transport.TransportServer;
import org.apache.mina.common.IoSession;
import org.apache.mina.handler.demux.DemuxingIoHandler;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.InstantiationStrategy;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides support for the server-side of the remote shell protocol.
 *
 * @version $Rev$ $Date$
 */
@Component(role=RshServer.class, instantiationStrategy=InstantiationStrategy.PER_LOOKUP)
public class RshServer
    implements Initializable
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private PlexusContainer container;

    private ClassWorld classWorld;

    private ScheduledThreadPoolExecutor scheduler;

    @Requirement
    private CryptoContext crypto;

    @Requirement
    private TransportFactoryLocator locator;

    private TransportServer server;

    private final UUID securityToken = UUID.randomUUID();

    public void initialize() throws InitializationException {
        classWorld = container.getContainerRealm().getWorld();

        ThreadFactory tf = new NamedThreadFactory(getClass());
        scheduler = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), tf);
    }

    public void bind(final URI location) throws Exception {
        TransportFactory factory = locator.locate(location);

        server = factory.bind(location, new ServerHandler());

        log.debug("Bound to: {}", location);
    }

    public void close() {
        server.close();
    }

    //
    // SessionState
    //

    /**
     * Session binding helper for {@link SessionState} instances.
     */
    private static final SessionAttributeBinder<SessionState> SESSION_STATE = new SessionAttributeBinder<SessionState>(SessionState.class);

    /**
     * Container for various bits of state we are tracking.
     */
    private class SessionState
    {
        /** The remote client's unique identifier. */
        public final UUID id = UUID.randomUUID();

        /** The remove client's public key. */
        public PublicKey pk;

        /** The shared security token */
        public UUID sectoken;

        /** The remote client's logged in username. */
        public String username;

        /** The container which the remote shell is running in. */
        public RemoteShellContainer container;

        /** The I/O context for the remote shell. */
        public RemoteIO io;

        /** The environment for the remote shell. */
        public Environment env;

        /** The remote shell instance. */
        public RemoteShell shell;

        public void destroy() {
            shell.close();

            container.disposeAllComponents();
        }

        public int hashCode() {
            return id.hashCode();
        }

        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("id", id)
                    .append("username", username)
                    .toString();
        }
    }

    private class ServerHandler
        extends DemuxingIoHandler
    {
        public ServerHandler() throws Exception {
            register(new ConnectHandler());
            register(new LoginHandler());
            register(new OpenShellHandler());
            register(new CloseShellHandler());
            register(new ExecuteHandler());
            register(new EchoHandler());
        }

        public void register(final MessageHandler handler) {
            assert handler != null;

            addMessageHandler(handler.getType(), handler);
        }

        /*
        @Override
        public void messageReceived(final IoSession session, final Object obj) throws Exception {
            assert session != null;
            assert obj != null;

            SessionState state = SESSION_STATE.lookup(session, null);

            if (state != null && securityToken.equals(state.sectoken)) {
                log.debug("Message is already authenticated");

                super.messageReceived(session, obj);
            }
            else if (obj instanceof HandshakeMessage) {
                log.debug("Message requires handshake/authentication");

                super.messageReceived(session, obj);
            }
            else {
                // If we get to here, then the message is not valid, so complain, then kill the session
                log.error("Invalid message: {}", obj);

                //
                // TODO: See if we can just toss an IOException here instead?
                //

                session.close();
            }
        }
        */
        
        @Override
        public void sessionOpened(final IoSession session) throws Exception {
            assert session != null;

            // Schedule a task to timeout the handshake process
            scheduleTimeout(session, AUTH_TIMEOUT);
        }

        @Override
        public void sessionClosed(final IoSession session) throws Exception {
            assert session != null;

            SessionState state = SESSION_STATE.unbind(session);

            // If there is still state bound then clean it up
            if (state != null) {
                log.warn("Delinquent state detected: {}", state);

                try {
                    state.destroy();
                }
                catch (Exception e) {
                    log.warn("Failed to clean up after delinquent state", e);
                }
            }
        }
    }

    private class ConnectHandler
        extends MessageHandlerSupport<ConnectMessage>
    {
        public ConnectHandler() {
            super(RshMessage.Type.CONNECT);
        }

        public void messageReceived(IoSession session, ConnectMessage message) throws Exception {
            // Try to cancel the timeout task
            if (!cancelTimeout(session)) {
                log.warn("Aborting handshake processing; timeout has triggered");
            }
            else {
                // Setup the initial client state
                SessionState state = SESSION_STATE.bind(session, new SessionState());
                log.info("Initiating state for client: {}", state.id);

                // Hold on to the client's public key
                state.pk = message.getPublicKey();

                // Reply to the client with some details about the connection
                ConnectMessage.Result reply = new ConnectMessage.Result(state.id, crypto.getPublicKey());
                reply.setCorrelationId(message.getId());
                session.write(reply);

                // Schedule a task to timeout the login process
                scheduleTimeout(session, AUTH_TIMEOUT);
            }
        }
    }

    private class LoginHandler
        extends MessageHandlerSupport<LoginMessage>
    {
        protected LoginHandler() {
            super(RshMessage.Type.LOGIN);
        }

        public void messageReceived(IoSession session, LoginMessage message) throws Exception {
            // Try to cancel the timeout task
            if (!cancelTimeout(session)) {
                log.warn("Aborting login processing; timeout has triggered");
            }
            else {
                String username = message.getUsername();
                String password = message.getPassword();

                SessionState state = SESSION_STATE.lookup(session);

                //
                // TODO: Implement something else...
                //

                // Mark the session as authenticated (which is done by setting the sectoken)
                state.sectoken = securityToken;

                // Remember our username
                state.username = username;

                log.info("Successfull authentication for user: {}, at location: {}", username, session.getRemoteAddress());

                LoginMessage.Success reply = new LoginMessage.Success();
                reply.setCorrelationId(message.getId());
                session.write(reply);
            }
        }
    }

    private class OpenShellHandler
        extends MessageHandlerSupport<OpenShellMessage>
    {
        protected OpenShellHandler() {
            super(RshMessage.Type.OPEN_SHELL);
        }

        public void messageReceived(IoSession session, OpenShellMessage message) throws Exception {
            SessionState state = SESSION_STATE.lookup(session);

            // Create a new container which will be the parent for our remote shells
            state.container = RemoteShellContainer.create(classWorld);

            // Setup the I/O context (w/o auto-flushing)
            state.io = new RemoteIO(session);
            IOLookup.set(state.container, state.io);

            // Setup shell environemnt
            state.env = new DefaultEnvironment(state.io);
            EnvironmentLookup.set(state.container, state.env);

            // Create a new shell instance
            state.shell = (RemoteShell) state.container.lookup(RemoteShell.class);

            //
            // TODO: Send a meaningful response
            //

            EchoMessage reply = new EchoMessage("OPEN SHELL SUCCESS");
            reply.setCorrelationId(message.getId());
            session.write(reply);
        }
    }

    private class CloseShellHandler
        extends MessageHandlerSupport<CloseShellMessage>
    {
        protected CloseShellHandler() {
            super(RshMessage.Type.CLOSE_SHELL);
        }

        public void messageReceived(IoSession session, CloseShellMessage message) throws Exception {
            SessionState state = SESSION_STATE.unbind(session);

            //
            // TODO: This should just clean up the bits related to shell muck...
            //

            state.destroy();

            //
            // TODO: Send a meaningful response
            //

            EchoMessage reply = new EchoMessage("CLOSE SHELL SUCCESS");
            reply.setCorrelationId(message.getId());
            session.write(reply);
        }
    }

    private class ExecuteHandler
        extends MessageHandlerSupport<ExecuteMessage>
    {
        protected ExecuteHandler() {
            super(RshMessage.Type.EXECUTE);
        }

        public void messageReceived(IoSession session, ExecuteMessage message) throws Exception {
            SessionState state = SESSION_STATE.lookup(session);

            // Need to make sure that the execuing thread has the right I/O and environment in context
            IOLookup.set(state.container, state.io);
            EnvironmentLookup.set(state.container, state.env);

            ExecuteMessage.Result reply;

            try {
                Object result = message.execute(state.shell);

                log.debug("Result: {}", result);

                reply = new ExecuteMessage.Result(result);
            }
            catch (Notification n) {
                log.debug("Notification: " + n);

                reply = new ExecuteMessage.Notification(n);
            }
            catch (Throwable t) {
                log.debug("Fault: " + t);

                reply = new ExecuteMessage.Fault(t);
            }

            reply.setCorrelationId(message.getId());
            session.write(reply);
        }
    }

    private class EchoHandler
        extends MessageHandlerSupport<EchoMessage>
    {
        protected EchoHandler() {
            super(RshMessage.Type.ECHO);
        }

        public void messageReceived(IoSession session, EchoMessage message) throws Exception {
            EchoMessage reply = new EchoMessage(message.getText());
            reply.setCorrelationId(message.getId());
            session.write(reply);
        }
    }

    //
    // Timeout Support
    //

    //
    // TODO: Move this timeout stuff to a component, a few things need this functionality, probably more than I can think of too..
    //

    private static final SessionAttributeBinder<ScheduledFuture> TIMEOUT_BINDER = new SessionAttributeBinder<ScheduledFuture>(RshServer.class, "timeout");

    private static final Duration AUTH_TIMEOUT = new Duration(10, TimeUnit.SECONDS);

    private ScheduledFuture scheduleTimeout(final IoSession session, final Duration timeout) {
        assert session != null;
        assert timeout != null;

        Runnable task = new Runnable() {
            public void run() {
                log.error("Timeout waiting for handshake or authentication from: {}", session.getRemoteAddress());

                session.close();
            }
        };

        ScheduledFuture tf = scheduler.schedule(task, timeout.value, timeout.unit);

        TIMEOUT_BINDER.rebind(session, tf);

        return tf;
    }

    private boolean cancelTimeout(final IoSession session) {
        assert session != null;

        ScheduledFuture tf = TIMEOUT_BINDER.lookup(session);

        return tf.cancel(false);
    }
}