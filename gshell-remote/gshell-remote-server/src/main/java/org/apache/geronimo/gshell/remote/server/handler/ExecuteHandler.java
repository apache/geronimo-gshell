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

import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.notification.Notification;
import org.apache.geronimo.gshell.remote.message.ExecuteMessage;
import org.apache.geronimo.gshell.remote.server.RemoteShellContextHolder;
import org.apache.geronimo.gshell.shell.ShellContext;
import org.apache.geronimo.gshell.whisper.transport.Session;

/**
 * Server handler for {@link ExecuteMessage} messages.
 *
 * @version $Rev$ $Date$
 */
public class ExecuteHandler
    extends ServerMessageHandlerSupport<ExecuteMessage>
{
    public ExecuteHandler() {
        super(ExecuteMessage.class);
    }

    public void handle(final Session session, final ServerSessionContext context, final ExecuteMessage message) throws Exception {
        assert session != null;
        assert context != null;
        assert message != null;

        ShellContext shellContext = new ShellContext() {
            public IO getIo() {
                return context.io;
            }

            public Variables getVariables() {
                return context.variables;
            }
        };

        ExecuteMessage.Result reply;
        
        RemoteShellContextHolder.setContext(shellContext);

        try {
            try {
                Object result = message.execute(context.shell);

                log.debug("Result: {}", result);

                reply = new ExecuteMessage.Result(result);
            }
            catch (Notification n) {
                log.debug("Notification: " + n);

                reply = new ExecuteMessage.NotificationResult(n);
            }
            catch (Throwable t) {
                log.debug("Failure: " + t);

                reply = new ExecuteMessage.FailureResult(t);
            }
        }
        finally {
            RemoteShellContextHolder.clearContext();
        }

        reply.setCorrelationId(message.getId());
        session.send(reply);
    }
}
