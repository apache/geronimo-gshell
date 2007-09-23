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
import org.apache.geronimo.gshell.command.IO;
import org.apache.geronimo.gshell.common.Duration;
import org.apache.geronimo.gshell.common.NamedThreadFactory;
import org.apache.geronimo.gshell.common.Notification;
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
import org.apache.geronimo.gshell.remote.stream.SessionInputStream;
import org.apache.geronimo.gshell.remote.stream.SessionOutputStream;
import org.apache.geronimo.gshell.shell.Environment;
import org.apache.mina.common.IoSession;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
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
    //
    // TODO: Introduce a context object which we can stuff any kinda of data we want into for the client connection... and have one binder, etc...
    //

    private static final SessionAttributeBinder<PublicKey> CLIENT_KEY_BINDER = new SessionAttributeBinder<PublicKey>(RshServerHandler.class, "clientpk");

    private static final SessionAttributeBinder<UUID> SECTOKEN = new SessionAttributeBinder<UUID>(RshServerHandler.class, "sectoken");

    private static final SessionAttributeBinder<ScheduledFuture> TIMEOUT_BINDER = new SessionAttributeBinder<ScheduledFuture>(RshServerHandler.class, "timeout");

    @Requirement
    private PlexusContainer parentContainer;

    @Requirement
    private CryptoContext crypto;

    @Requirement
    private UserAuthenticator userAuthenticator;

    private ScheduledThreadPoolExecutor scheduler;

    private UUID securityToken;

    public void initialize() throws InitializationException {
        setVisitor(new Visitor());

        ThreadFactory tf = new NamedThreadFactory(getClass());

        scheduler = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), tf);

        securityToken = UUID.randomUUID();
    }

    @Override
    public void sessionOpened(final IoSession session) throws Exception {
        assert session != null;

        // Schedule a task to timeout the handshake process
        scheduleTimeout(session);
    }

    @Override
    public void messageReceived(final IoSession session, final Object obj) throws Exception {
        assert session != null;
        assert obj != null;

        UUID token = SECTOKEN.lookup(session, null);

        if (securityToken.equals(token)) {
            super.messageReceived(session, obj);
        }
        else if (obj instanceof HandshakeMessage) {
            super.messageReceived(session, obj);
        }
        else {
            // If we get to here, then the message is not valid, so complain, then kill the session
            log.error("Unauthenticated message: {}", obj);

            session.close();
        }
    }

    //
    // Timeout Support
    //

    //
    // TODO: Move this timeout stuff to a component, a few things need this functionality, probably more than I can think of too..
    //
    
    private static final Duration AUTH_TIMEOUT = new Duration(15, TimeUnit.SECONDS);

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

    private ScheduledFuture scheduleTimeout(final IoSession session) {
        return scheduleTimeout(session, AUTH_TIMEOUT);
    }

    private boolean cancelTimeout(final IoSession session) {
        assert session != null;

        ScheduledFuture tf = TIMEOUT_BINDER.lookup(session);

        return tf.cancel(false);
    }

    //
    // MessageVisitor
    //

    //
    // TODO: Introduce a context object which we can stuff any kinda of data we want into for the remote shell session... and have one binder, etc...
    //

    private static final SessionAttributeBinder<IO> IO_BINDER = new SessionAttributeBinder<IO>(IO.class);

    private static final SessionAttributeBinder<Environment> ENV_BINDER = new SessionAttributeBinder<Environment>(Environment.class);

    private static final SessionAttributeBinder<RemoteShell> SHELL_BINDER = new SessionAttributeBinder<RemoteShell>(RemoteShell.class);

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
                PublicKey key = msg.getClientKey();

                // Stuff the remote public key into the session
                CLIENT_KEY_BINDER.bind(session, key);

                //
                // TODO: Do we want to pass the client back some token which it needs to put onto messages that are sent for more security?
                //

                // And then send back our public key to the remote client
                msg.reply(new ConnectMessage.Result(crypto.getPublicKey()));

                // Schedule a task to timeout the login process
                scheduleTimeout(session);
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
                    // Mark the session as authenticated
                    SECTOKEN.bind(session, securityToken);

                    log.info("Successfull authentication for user: {}, at location: {}", username, session.getRemoteAddress());

                    msg.reply(new LoginMessage.Success());
                }
            }
        }

        //
        // Remote Shell Session
        //

        private RemoteShellContainer createContainer() throws PlexusContainerException {
            // Create a new container which will be the parent for our remote shells
            ContainerConfiguration config = new DefaultContainerConfiguration();

            String name = "gshell.remote-shell:" + UUID.randomUUID();

            config.setName(name);

            config.setClassWorld(parentContainer.getContainerRealm().getWorld());

            return new RemoteShellContainer(config);
        }

        @Override
        public void visitOpenShell(final IoSession session, final OpenShellMessage msg) throws Exception {
            log.info("OPEN SHELL: {}", msg);

            RemoteShellContainer shellContainer = createContainer();
            RemoteShellContainer.BINDER.bind(session, shellContainer);

            // Setup the I/O context (w/o auto-flushing)
            IO io = new IO(SessionInputStream.BINDER.lookup(session), SessionOutputStream.BINDER.lookup(session), false);

            //
            // TODO: We need to set the verbosity of this I/O context as specified by the client
            //

            IOLookup.set(shellContainer, io);
            IO_BINDER.bind(session, io);

            // Setup shell environemnt
            Environment env = new DefaultEnvironment(io);
            EnvironmentLookup.set(shellContainer, env);
            ENV_BINDER.bind(session, env);

            // Create a new shell instance
            RemoteShell shell = (RemoteShell) shellContainer.lookup(RemoteShell.class);
            SHELL_BINDER.bind(session, shell);

            //
            // TODO: Send a meaningful response
            //

            msg.reply(new EchoMessage("OPEN SHELL SUCCESS"));
        }

        @Override
        public void visitCloseShell(final IoSession session, final CloseShellMessage msg) throws Exception {
            log.info("CLOSE SHELL: {}", msg);

            log.info("Closing shell");

            RemoteShell shell = SHELL_BINDER.unbind(session);

            shell.close();

            log.info("Unbinding resources");

            IO_BINDER.unbind(session);

            ENV_BINDER.unbind(session);

            log.info("Destroying container");

            RemoteShellContainer shellContainer = RemoteShellContainer.BINDER.unbind(session);
            shellContainer.disposeAllComponents();

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

            RemoteShell shell = SHELL_BINDER.lookup(session);

            try {
                //
                // TODO: Need to find a better place to stash this me thinks...
                //

                RemoteShellContainer shellContainer = RemoteShellContainer.BINDER.lookup(session);

                IO io = IO_BINDER.lookup(session);
                IOLookup.set(shellContainer, io);

                Environment env = ENV_BINDER.lookup(session);
                EnvironmentLookup.set(shellContainer, env);

                Object result = msg.execute(shell);

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
}