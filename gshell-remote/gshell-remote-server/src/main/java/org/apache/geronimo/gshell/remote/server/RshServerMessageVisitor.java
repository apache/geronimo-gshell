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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.geronimo.gshell.DefaultEnvironment;
import org.apache.geronimo.gshell.ExitNotification;
import org.apache.geronimo.gshell.command.IO;
import org.apache.geronimo.gshell.lookup.EnvironmentLookup;
import org.apache.geronimo.gshell.lookup.IOLookup;
import org.apache.geronimo.gshell.remote.message.CloseShellMessage;
import org.apache.geronimo.gshell.remote.message.EchoMessage;
import org.apache.geronimo.gshell.remote.message.ExecuteMessage;
import org.apache.geronimo.gshell.remote.message.MessageVisitor;
import org.apache.geronimo.gshell.remote.message.MessageVisitorSupport;
import org.apache.geronimo.gshell.remote.message.OpenShellMessage;
import org.apache.geronimo.gshell.shell.Environment;
import org.apache.mina.common.IoSession;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.factory.ComponentFactory;

/**
 * Defines the logic for server-side message processing.
 *
 * @version $Rev$ $Date$
 */
@Component(role=MessageVisitor.class, hint="server")
public class RshServerMessageVisitor
    extends MessageVisitorSupport
{
    //
    // TODO: If/when we want to add a state-machine to keep things in order, add server-side here.
    //

    @Requirement
    private PlexusContainer container;

    //
    // Remote Shell Access
    //

    private ClassWorld getClassWorld() {
        return container.getContainerRealm().getWorld();
    }

    private RemoteShell createRemoteShell(final IoSession session) throws Exception {
        assert session != null;

        log.info("Creating remote shell");
        
        // We need to create a new container (not a child container) to allow the remote shell a full unpollute namespace for components
        ContainerConfiguration config = new DefaultContainerConfiguration();
        config.setName("gshell.rsh");
        config.setClassWorld(getClassWorld());
        PlexusContainer container = new DefaultPlexusContainer(config);
        session.setAttribute(PlexusContainer.class.getName(), container);

        // Setup the I/O context (w/o auto-flushing)
        IO io = new IO(getInputStream(session), getOutputStream(session), false);

        //
        // FIXME: We need to set the verbosity of this I/O context as specified by the client
        //

        IOLookup.set(container, io);
        session.setAttribute(IO.class.getName(), io);

        // Setup shell environemnt
        Environment env = new DefaultEnvironment(io);
        EnvironmentLookup.set(container, env);
        session.setAttribute(Environment.class.getName(), env);

        // Create a new shell instance
        RemoteShell shell = (RemoteShell) container.lookup(RemoteShell.class);

        log.info("Created remote shell: {}", shell);
        
        return shell;
    }

    private RemoteShell getRemoteShell(final IoSession session) {
        assert session != null;

        RemoteShell shell = (RemoteShell) session.getAttribute(RemoteShell.SESSION_KEY);

        if (shell == null) {
            throw new IllegalStateException("Remote shell not bound");
        }

        return shell;
    }

    private void setRemoteShell(final IoSession session, final RemoteShell shell) {
        assert session != null;
        assert shell != null;

        // Make sure that no session already exists
        Object obj = session.getAttribute(RemoteShell.SESSION_KEY);

        if (obj != null) {
            throw new IllegalStateException("Remote shell already bound");
        }

        session.setAttribute(RemoteShell.class.getName(), shell);
    }

    private void unsetRemoteShell(final IoSession session) {
        assert session != null;

        Object obj = session.getAttribute(RemoteShell.SESSION_KEY);

        // Complain if no remote shell has been bound
        if (obj != null) {
            log.warn("Ignoring request to unset remote shell; no shell is bound");
        }
        else {
            session.removeAttribute(RemoteShell.SESSION_KEY);
        }
    }

    //
    // MessageVisitor
    //

    public void visitEcho(final EchoMessage msg) throws Exception {
        assert msg != null;

        log.info("ECHO: {}", msg);

        String text = msg.getText();

        msg.reply(new EchoMessage(text));
    }

    public void visitOpenShell(final OpenShellMessage msg) throws Exception {
        assert msg != null;

        log.info("OPEN SHELL: {}", msg);

        IoSession session = msg.getSession();

        // Create a new shell instance and bind it to the session
        RemoteShell shell = createRemoteShell(session);
        setRemoteShell(session, shell);

        //
        // TODO: Send a meaningful response
        //

        msg.reply(new EchoMessage("OPEN SHELL SUCCESS"));
    }

    public void visitCloseShell(final CloseShellMessage msg) throws Exception {
        assert msg != null;

        log.info("CLOSE SHELL: {}", msg);

        IoSession session = msg.getSession();

        RemoteShell shell = getRemoteShell(session);

        shell.close();

        unsetRemoteShell(session);

        //
        // TODO: Send a meaningful response
        //

        msg.reply(new EchoMessage("CLOSE SHELL SUCCESS"));
    }

    private ExecutorService executorService = Executors.newCachedThreadPool();

    public void visitExecute(final ExecuteMessage msg) throws Exception {
        assert msg != null;

        log.info("EXECUTE (QUEUE): {}", msg);

        final IoSession session = msg.getSession();

        final RemoteShell shell = getRemoteShell(session);

        Runnable executeCommandTask = new Runnable() {
            public void run() {
                log.info("EXECUTE: {}", msg);
                
                try {
                    //
                    // TODO: Need to find a better place to stash this me thinks...
                    //
                    
                    // Need to make sure we bind the correct bits into the lookups, since they are thread specific
                    PlexusContainer container = (PlexusContainer) session.getAttribute(PlexusContainer.class.getName());

                    IO io = (IO) session.getAttribute(IO.class.getName());
                    IOLookup.set(container, io);

                    Environment env = (Environment) session.getAttribute(Environment.class.getName());
                    EnvironmentLookup.set(container, env);

                    Object result = msg.execute(shell);

                    msg.reply(new ExecuteMessage.Result(result));
                }
                catch (ExitNotification n) {
                    //
                    // TODO: Send client message with this detail...
                    //

                    log.info("Remote shell requested exit: {}", n);

                    session.close();
                }
                catch (Throwable t) {
                    log.error("Unhandled failure; sending to client: " + t, t);

                    msg.reply(new ExecuteMessage.Fault(t));
                }
            }
        };

        executorService.submit(executeCommandTask);
    }
}