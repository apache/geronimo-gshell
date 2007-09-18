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

import org.apache.geronimo.gshell.DefaultEnvironment;
import org.apache.geronimo.gshell.command.IO;
import org.apache.geronimo.gshell.lookup.EnvironmentLookup;
import org.apache.geronimo.gshell.lookup.IOLookup;
import org.apache.geronimo.gshell.remote.message.CloseShellMessage;
import org.apache.geronimo.gshell.remote.message.EchoMessage;
import org.apache.geronimo.gshell.remote.message.ExecuteMessage;
import org.apache.geronimo.gshell.remote.message.HandShakeMessage;
import org.apache.geronimo.gshell.remote.message.MessageVisitor;
import org.apache.geronimo.gshell.remote.message.MessageVisitorSupport;
import org.apache.geronimo.gshell.remote.message.OpenShellMessage;
import org.apache.geronimo.gshell.shell.Environment;
import org.apache.mina.common.IoSession;
import org.codehaus.plexus.PlexusContainer;
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
    @Requirement
    private PlexusContainer container;

    //
    // Remote Shell Access
    //

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

    public void visitHandShake(final HandShakeMessage msg) throws Exception {
        assert msg != null;

        log.info("HANDSHAKE: {}", msg);

        //
        // TODO:
        //
        
        msg.reply(new EchoMessage("SUCCESS"));
    }

    public void visitOpenShell(final OpenShellMessage msg) throws Exception {
        assert msg != null;

        log.info("OPEN SHELL: {}", msg);

        IoSession session = msg.getSession();

        // Setup the I/O context
        IO io = new IO(getInputStream(session), getOutputStream(session));
        IOLookup ioLookup = (IOLookup) container.lookup(ComponentFactory.class, IOLookup.class.getSimpleName());
        ioLookup.set(io);

        // Setup the shell environemnt
        Environment env = new DefaultEnvironment(io);
        EnvironmentLookup envLookup = (EnvironmentLookup) container.lookup(ComponentFactory.class, EnvironmentLookup.class.getSimpleName());
        envLookup.set(env);

        // Create a new shell instance
        RemoteShell shell = (RemoteShell) container.lookup(RemoteShell.class);

        // Bind it to the session
        setRemoteShell(session, shell);

        //
        // TODO: Send response
        //
    }

    public void visitCloseShell(final CloseShellMessage msg) throws Exception {
        assert msg != null;

        log.info("CLOSE SHELL: {}", msg);

        IoSession session = msg.getSession();

        RemoteShell shell = getRemoteShell(session);

        shell.close();

        unsetRemoteShell(session);

        //
        // TODO: Send response
        //
    }

    public void visitExecute(final ExecuteMessage msg) throws Exception {
        assert msg != null;

        log.info("EXECUTE: {}", msg);

        IoSession session = msg.getSession();

        RemoteShell shell = getRemoteShell(session);

        Object result = msg.execute(shell);

        //
        // TODO: Send response
        //
    }
}