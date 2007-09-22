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
import java.util.Date;
import java.util.UUID;

import org.apache.geronimo.gshell.DefaultEnvironment;
import org.apache.geronimo.gshell.command.IO;
import org.apache.geronimo.gshell.common.Notification;
import org.apache.geronimo.gshell.lookup.EnvironmentLookup;
import org.apache.geronimo.gshell.lookup.IOLookup;
import org.apache.geronimo.gshell.remote.RemoteShell;
import org.apache.geronimo.gshell.remote.message.MessageVisitor;
import org.apache.geronimo.gshell.remote.message.MessageVisitorSupport;
import org.apache.geronimo.gshell.remote.message.rsh.CloseShellMessage;
import org.apache.geronimo.gshell.remote.message.rsh.EchoMessage;
import org.apache.geronimo.gshell.remote.message.rsh.ExecuteMessage;
import org.apache.geronimo.gshell.remote.message.rsh.OpenShellMessage;
import org.apache.geronimo.gshell.remote.session.SessionAttributeBinder;
import org.apache.geronimo.gshell.remote.stream.SessionInputStream;
import org.apache.geronimo.gshell.remote.stream.SessionOutputStream;
import org.apache.geronimo.gshell.shell.Environment;
import org.apache.mina.common.IoSession;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

/**
 * Defines the logic for server-side message processing.
 *
 * @version $Rev$ $Date$
 */
@Component(role=MessageVisitor.class, hint="server")
public class RshServerMessageVisitor
    extends MessageVisitorSupport
{
    private static final SessionAttributeBinder<PlexusContainer> CONTAINER_BINDER = new SessionAttributeBinder<PlexusContainer>(PlexusContainer.class);

    private static final SessionAttributeBinder<IO> IO_BINDER = new SessionAttributeBinder<IO>(IO.class);

    private static final SessionAttributeBinder<Environment> ENV_BINDER = new SessionAttributeBinder<Environment>(Environment.class);

    private static final SessionAttributeBinder<RemoteShell> SHELL_BINDER = new SessionAttributeBinder<RemoteShell>(RemoteShell.class);
    
    //
    // TODO: If/when we want to add a state-machine to keep things in order, add server-side here.
    //

    @Requirement
    private PlexusContainer parentContainer;

    //
    // MessageVisitor
    //

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

    private DefaultPlexusContainer createContainer() throws PlexusContainerException {
        // Create a new container which will be the parent for our remote shells
        ContainerConfiguration config = new DefaultContainerConfiguration();

        String name = "gshell.remote-shell:" + UUID.randomUUID();

        config.setName(name);

        config.setClassWorld(parentContainer.getContainerRealm().getWorld());

        return new DefaultPlexusContainer(config);
    }

    public void visitOpenShell(final OpenShellMessage msg) throws Exception {
        assert msg != null;

        log.info("OPEN SHELL: {}", msg);

        IoSession session = msg.getSession();

        PlexusContainer childContainer = createContainer();
        CONTAINER_BINDER.bind(session, childContainer);

        // Setup the I/O context (w/o auto-flushing)
        IO io = new IO(SessionInputStream.BINDER.lookup(session), SessionOutputStream.BINDER.lookup(session), false);

        //
        // FIXME: We need to set the verbosity of this I/O context as specified by the client
        //

        IOLookup.set(childContainer, io);
        IO_BINDER.bind(session, io);

        // Setup shell environemnt
        Environment env = new DefaultEnvironment(io);
        EnvironmentLookup.set(childContainer, env);
        ENV_BINDER.bind(session, env);

        // Create a new shell instance
        RemoteShell shell = (RemoteShell) childContainer.lookup(RemoteShell.class);
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

        PlexusContainer childContainer = CONTAINER_BINDER.unbind(session);

        //
        // FIXME: This won't work... it kills our class realm... :-(
        //
        // childContainer.dispose();
        
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

            PlexusContainer container = CONTAINER_BINDER.lookup(session);

            IO io = IO_BINDER.lookup(session);
            IOLookup.set(container, io);

            Environment env = ENV_BINDER.lookup(session);
            EnvironmentLookup.set(container, env);

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