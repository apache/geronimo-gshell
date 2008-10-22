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

import org.apache.geronimo.gshell.remote.message.CloseShellMessage;
import org.apache.geronimo.gshell.shell.ShellContext;
import org.apache.geronimo.gshell.shell.ShellContextHolder;
import org.apache.geronimo.gshell.whisper.transport.Session;

/**
 * Server handler for {@link CloseShellMessage} messages.
 *
 * @version $Rev$ $Date$
 */
public class CloseShellHandler
    extends ServerMessageHandlerSupport<CloseShellMessage>
{
    public CloseShellHandler() {
        super(CloseShellMessage.class);
    }

    public void handle(final Session session, final ServerSessionContext context, final CloseShellMessage message) throws Exception {
        assert session != null;
        assert context != null;
        assert message != null;

        ShellContext prevContext = ShellContextHolder.get(true);
        ShellContextHolder.set(context.shellContext);

        try {
            context.close();
        }
        finally {
            ShellContextHolder.set(prevContext);
        }

        CloseShellMessage.Result reply = new CloseShellMessage.Result();
        reply.setCorrelationId(message.getId());
        session.send(reply);
    }
}
