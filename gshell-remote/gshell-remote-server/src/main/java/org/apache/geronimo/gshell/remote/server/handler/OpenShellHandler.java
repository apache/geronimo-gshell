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

package org.apache.geronimo.gshell.remote.server.handler;

import org.apache.geronimo.gshell.application.DefaultShellContext;
import org.apache.geronimo.gshell.remote.RemoteShell;
import org.apache.geronimo.gshell.remote.message.EchoMessage;
import org.apache.geronimo.gshell.remote.message.OpenShellMessage;
import org.apache.geronimo.gshell.remote.server.RemoteIO;
import org.apache.geronimo.gshell.remote.server.RemoteShellContainer;
import org.apache.geronimo.gshell.whisper.transport.Session;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
@Component(role=ServerMessageHandler.class, hint="open-shell")
public class OpenShellHandler
    extends ServerMessageHandlerSupport<OpenShellMessage>
{
    @Requirement
    private PlexusContainer container;

    public OpenShellHandler() {
        super(OpenShellMessage.class);
    }

    public void handle(final Session session, final ServerSessionContext context, final OpenShellMessage message) throws Exception {
        // Create a new container which will be the parent for our remote shells
        ClassWorld classWorld = container.getContainerRealm().getWorld();
        context.container = RemoteShellContainer.create(classWorld);

        // Setup the I/O context (w/o auto-flushing)
        context.io = new RemoteIO(session);
        // FIXME: Need to find out what to do here, w/o this lookup
        // IOLookup.set(context.container, context.io);

        // Setup shell environemnt
        context.env = new DefaultShellContext(context.io);
        // FIXME: Need to find out what to do here, w/o this lookup
        // EnvironmentLookup.set(context.container, context.env);

        // Create a new shell instance
        context.shell = (RemoteShell) context.container.lookup(RemoteShell.class);

        //
        // TODO: Send a meaningful response
        //

        EchoMessage reply = new EchoMessage("OPEN SHELL SUCCESS");
        reply.setCorrelationId(message.getId());
        session.send(reply);
    }
}
