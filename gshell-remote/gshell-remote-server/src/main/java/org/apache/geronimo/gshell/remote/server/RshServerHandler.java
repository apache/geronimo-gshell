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

import java.io.PrintWriter;
import java.security.PublicKey;
import java.util.Date;
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
import org.apache.geronimo.gshell.remote.message.MessageHandler;
import org.apache.geronimo.gshell.remote.message.MessageVisitorSupport;
import org.apache.geronimo.gshell.remote.message.rsh.CloseShellMessage;
import org.apache.geronimo.gshell.remote.message.rsh.ConnectMessage;
import org.apache.geronimo.gshell.remote.message.rsh.EchoMessage;
import org.apache.geronimo.gshell.remote.message.rsh.ExecuteMessage;
import org.apache.geronimo.gshell.remote.message.rsh.HandshakeMessage;
import org.apache.geronimo.gshell.remote.message.rsh.LoginMessage;
import org.apache.geronimo.gshell.remote.message.rsh.OpenShellMessage;
import org.apache.geronimo.gshell.remote.server.auth.UserAuthenticator;
import org.apache.geronimo.gshell.remote.session.SessionAttributeBinder;
import org.apache.geronimo.gshell.remote.stream.SessionOutputStream;
import org.apache.geronimo.gshell.shell.Environment;
import org.apache.mina.common.IoSession;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

/**
 * Provides the server-side message handling for the GShell rsh protocol.
 *
 * @version $Rev$ $Date$
 */
@Component(role=MessageHandler.class, hint="server")
public class RshServerHandler
    extends MessageHandler
    implements Initializable
{
    @Requirement
    private PlexusContainer container;

    private ClassWorld classWorld;

    @Requirement
    private CryptoContext crypto;

    @Requirement
    private UserAuthenticator userAuthenticator;

    private ScheduledThreadPoolExecutor scheduler;

    private final UUID securityToken = UUID.randomUUID();

    public void initialize() throws InitializationException {
        setVisitor(new Visitor());

        //
        // FIXME: I'm not sure this is really the correct thing to be doing... but I need a classworld to
        //        to boot up remote shell containers... so we steal it from here.
        //

        classWorld = container.getContainerRealm().getWorld();

        ThreadFactory tf = new NamedThreadFactory(getClass());
        scheduler = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), tf);
    }

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

    //
    // MessageVisitor
    //

    private class Visitor
        extends MessageVisitorSupport
    {
        //
        // Client-Server Handshake and Authentication
        //

        @Override
        public void visitConnect(final IoSession session, final ConnectMessage msg) throws Exception {
            log.debug("Processing handshake");
            
            // Try to cancel the timeout task
            if (!cancelTimeout(session)) {
                log.warn("Aborting handshake processing; timeout has triggered");
            }
            else {
                // Setup the initial client state
                SessionState state = SESSION_STATE.bind(session, new SessionState());

                log.info("Initiating state for client: {}", state.id);

                //
                // TODO: Should we track anything else from the session in our state, like the address or something?
                //
                
                // Hold on to the client's public key
                state.pk = msg.getPublicKey();

                // Reply to the client with some details about the connection
                msg.reply(new ConnectMessage.Result(state.id, crypto.getPublicKey()));

                // Schedule a task to timeout the login process
                scheduleTimeout(session, AUTH_TIMEOUT);
            }
        }

        @Override
        public void visitLogin(final IoSession session, final LoginMessage msg) throws Exception {
            log.debug("Processing login");

            // Try to cancel the timeout task
            if (!cancelTimeout(session)) {
                log.warn("Aborting login processing; timeout has triggered");
            }
            else {
                String username = msg.getUsername();

                String password = msg.getPassword();

                if (!userAuthenticator.authenticate(username, password)) {
                    log.error("Authentication failed for user: {}, at location: {}", username, session.getRemoteAddress());

                    String reason = "Failed to authenticate";

                    msg.reply(new LoginMessage.Failure(reason));
                }
                else {
                    SessionState state = SESSION_STATE.lookup(session);

                    // Mark the session as authenticated (which is done by setting the sectoken)
                    state.sectoken = securityToken;

                    // Remember our username
                    state.username = username;
                    
                    log.info("Successfull authentication for user: {}, at location: {}", username, session.getRemoteAddress());

                    msg.reply(new LoginMessage.Success());
                }
            }
        }

        //
        // Remote Shell Session
        //

        @Override
        public void visitOpenShell(final IoSession session, final OpenShellMessage msg) throws Exception {
            log.info("OPEN SHELL: {}", msg);

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

            msg.reply(new EchoMessage("OPEN SHELL SUCCESS"));
        }

        @Override
        public void visitCloseShell(final IoSession session, final CloseShellMessage msg) throws Exception {
            log.info("CLOSE SHELL: {}", msg);

            SessionState state = SESSION_STATE.unbind(session);

            //
            // TODO: This should just clean up the bits related to shell muck...
            //
            
            state.destroy();

            //
            // TODO: Send a meaningful response
            //

            msg.reply(new EchoMessage("CLOSE SHELL SUCCESS"));
        }

        //
        // Command Execution
        //

        @Override
        public void visitExecute(final IoSession session, final ExecuteMessage msg) throws Exception {
            log.info("EXECUTE: {}", msg);

            SessionState state = SESSION_STATE.lookup(session);

            // Need to make sure that the execuing thread has the right I/O and environment in context
            IOLookup.set(state.container, state.io);
            EnvironmentLookup.set(state.container, state.env);

            try {
                Object result = msg.execute(state.shell);

                log.debug("Result: {}", result);

                msg.reply(new ExecuteMessage.Result(result));
            }
            catch (Notification n) {
                log.debug("Notification: " + n);

                msg.reply(new ExecuteMessage.Notification(n));
            }
            catch (Throwable t) {
                log.debug("Fault: " + t);

                msg.reply(new ExecuteMessage.Fault(t));
            }
        }

        //
        // Testing & Debug
        //

        @Override
        public void visitEcho(final IoSession session, final EchoMessage msg) throws Exception {
            assert msg != null;

            log.info("ECHO: {}", msg);

            String text = msg.getText();

            if ("NOISE MAKER".equals(text)) {
                log.info("Making noise...");

                final PrintWriter out = new PrintWriter(SessionOutputStream.BINDER.lookup(session), false);

                new Thread("NOISE MAKER") {
                    public void run() {
                        while (true) {
                            out.println("FROM SERVER: " + new Date());
                            out.flush();

                            try {
                                Thread.sleep(5000);
                            }
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();
            }
            else if ("NO REPLY".equals(text)) {
                // ignore
            }
            else {
                msg.reply(new EchoMessage(text));
            }
        }
    }

    //
    // Timeout Support
    //

    //
    // TODO: Move this timeout stuff to a component, a few things need this functionality, probably more than I can think of too..
    //

    private static final SessionAttributeBinder<ScheduledFuture> TIMEOUT_BINDER = new SessionAttributeBinder<ScheduledFuture>(RshServerHandler.class, "timeout");

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