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

import org.apache.geronimo.gshell.common.Notification;
import org.apache.geronimo.gshell.lookup.EnvironmentLookup;
import org.apache.geronimo.gshell.lookup.IOLookup;
import org.apache.geronimo.gshell.remote.message.ExecuteMessage;
import org.apache.mina.common.IoSession;
import org.codehaus.plexus.component.annotations.Component;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
@Component(role=ServerMessageHandler.class, hint="execute")
public class ExecuteHandler
    extends ServerMessageHandlerSupport<ExecuteMessage>
{
    public ExecuteHandler() {
        super(ExecuteMessage.class);
    }

    public void handle(final IoSession session, final ServerSessionContext context, final ExecuteMessage message) throws Exception {
        // Need to make sure that the execuing thread has the right I/O and environment in context
        IOLookup.set(context.container, context.io);
        EnvironmentLookup.set(context.container, context.env);

        ExecuteMessage.Result reply;

        try {
            Object result = message.execute(context.shell);

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
