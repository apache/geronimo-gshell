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
import org.apache.geronimo.gshell.common.Notification;
import org.apache.geronimo.gshell.lookup.EnvironmentLookup;
import org.apache.geronimo.gshell.lookup.IOLookup;
import org.apache.geronimo.gshell.remote.RemoteShell;
import org.apache.geronimo.gshell.remote.crypto.CryptoContext;
import org.apache.geronimo.gshell.remote.message.Message;
import org.apache.geronimo.gshell.remote.message.MessageHandler;
import org.apache.geronimo.gshell.remote.message.MessageVisitorSupport;
import org.apache.geronimo.gshell.remote.message.rsh.CloseShellMessage;
import org.apache.geronimo.gshell.remote.message.rsh.EchoMessage;
import org.apache.geronimo.gshell.remote.message.rsh.ExecuteMessage;
import org.apache.geronimo.gshell.remote.message.rsh.HandShakeMessage;
import org.apache.geronimo.gshell.remote.message.rsh.LoginMessage;
import org.apache.geronimo.gshell.remote.message.rsh.OpenShellMessage;
import org.apache.geronimo.gshell.remote.server.auth.UserAuthenticator;
import org.apache.geronimo.gshell.remote.session.SessionAttributeBinder;
import org.apache.geronimo.gshell.remote.stream.SessionInputStream;
import org.apache.geronimo.gshell.remote.stream.SessionOutputStream;
import org.apache.geronimo.gshell.remote.util.NamedThreadFactory;
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
 * ???
 *
 * @version $Rev$ $Date$
 */
@Component(role=MessageHandler.class, hint="server")
public class RshServerHandler
    extends MessageHandler
    implements Initializable
{
    private static final SessionAttributeBinder<PublicKey> CLIENT_KEY_BINDER = new SessionAttributeBinder<PublicKey>(RshServerHandler.class, "clientPublicKey");

    private static final SessionAttributeBinder<UUID> AUTH_BINDER = new SessionAttributeBinder<UUID>(RshServerHandler.class, "authenticated");

    private static final SessionAttributeBinder<ScheduledFuture> TIMEOUT_BINDER = new SessionAttributeBinder<ScheduledFuture>(RshServerHandler.class);

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

        UUID token = AUTH_BINDER.lookup(session, null);

        if (securityToken.equals(token)) {
            super.messageReceived(session, obj);
        }
        else if (token != null) {
            log.error("Invalid security token: {}", token);

            session.close();
        }
        else if (obj instanceof HandShakeMessage) {
            doHandShake(session, (HandShakeMessage)obj);
        }
        else if (obj instanceof LoginMessage) {
            doLogin(session, (LoginMessage)obj);
        }
        else {
            // If we get to here, then the message is not valid, so complain, then kill the session
            log.error("Unauthenticated message: {}", obj);

            session.close();
        }
    }

    private void setSession(final IoSession session, final Message msg) {
        assert session != null;
        assert msg != null;

        // Prep the message for reply, this is normally done by the protocol handler, but that hasn't a chance to fire at this point
        msg.setSession(session);
        msg.freeze();
    }

    private void doHandShake(final IoSession session, final HandShakeMessage msg) throws Exception {
        assert session != null;
        assert msg != null;

        log.debug("Processing handshake");

        setSession(session, msg);

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
            msg.reply(new HandShakeMessage.Result(crypto.getPublicKey()));

            // Don't wait on the write future

            // Schedule a task to timeout the login process
            scheduleTimeout(session);
        }
    }

    private void doLogin(final IoSession session, final LoginMessage msg) throws Exception {
        assert session != null;
        assert msg != null;

        log.debug("Processing login");

        setSession(session, msg);

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
                AUTH_BINDER.bind(session, securityToken);

                log.info("Successfull authentication for user: {}, at location: {}", username, session.getRemoteAddress());

                msg.reply(new LoginMessage.Success());

                // Don't wait on the write future
            }
        }
    }

    //
    // Timeout Support
    //

    private ScheduledFuture scheduleTimeout(final IoSession session, final long l, final TimeUnit unit) {
        assert session != null;

        ScheduledFuture task = scheduler.schedule(new TimeoutTask(session), l, unit);
        TIMEOUT_BINDER.rebind(session, task);

        return task;
    }

    private ScheduledFuture scheduleTimeout(final IoSession session) {
        return scheduleTimeout(session, 30, TimeUnit.SECONDS);
    }

    private boolean cancelTimeout(final IoSession session) {
        assert session != null;

        ScheduledFuture timeoutTask = TIMEOUT_BINDER.lookup(session);

        return timeoutTask.cancel(false);
    }

    /**
     * Task to timeout sessions which fail to handshake or authenticate in a timely manner.
     */
    private class TimeoutTask
        implements Runnable
    {
        private final IoSession session;

        public TimeoutTask(final IoSession session) {
            assert session != null;

            this.session = session;
        }

        public void run() {
            log.error("Timeout waiting for handshake or authentication from: " + session.getRemoteAddress());

            session.close();
        }
    }

    //
    // MessageVisitor
    //

    private static final SessionAttributeBinder<IO> IO_BINDER = new SessionAttributeBinder<IO>(IO.class);

    private static final SessionAttributeBinder<Environment> ENV_BINDER = new SessionAttributeBinder<Environment>(Environment.class);

    private static final SessionAttributeBinder<RemoteShell> SHELL_BINDER = new SessionAttributeBinder<RemoteShell>(RemoteShell.class);

    private class Visitor
        extends MessageVisitorSupport
    {
        public void visitEcho(final EchoMessage msg) throws Exception {
            assert msg != null;

            log.info("ECHO: {}", msg);

            String text = msg.getText();

            //
            // HACK:
            //

            if ("NOISE MAKER".equals(text)) {
                log.info("Making noise...");

                final IoSession session = msg.getSession();
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

        private RemoteShellContainer createContainer() throws PlexusContainerException {
            // Create a new container which will be the parent for our remote shells
            ContainerConfiguration config = new DefaultContainerConfiguration();

            String name = "gshell.remote-shell:" + UUID.randomUUID();

            config.setName(name);

            config.setClassWorld(parentContainer.getContainerRealm().getWorld());

            return new RemoteShellContainer(config);
        }

        public void visitOpenShell(final OpenShellMessage msg) throws Exception {
            assert msg != null;

            log.info("OPEN SHELL: {}", msg);

            IoSession session = msg.getSession();

            RemoteShellContainer shellContainer = createContainer();
            RemoteShellContainer.BINDER.bind(session, shellContainer);

            // Setup the I/O context (w/o auto-flushing)
            IO io = new IO(SessionInputStream.BINDER.lookup(session), SessionOutputStream.BINDER.lookup(session), false);

            //
            // FIXME: We need to set the verbosity of this I/O context as specified by the client
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

        public void visitCloseShell(final CloseShellMessage msg) throws Exception {
            assert msg != null;

            log.info("CLOSE SHELL: {}", msg);

            IoSession session = msg.getSession();

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

        public void visitExecute(final ExecuteMessage msg) throws Exception {
            assert msg != null;

            log.info("EXECUTE: {}", msg);

            IoSession session = msg.getSession();

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
    }
}