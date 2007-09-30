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

import org.apache.geronimo.gshell.remote.crypto.CryptoContext;
import org.apache.geronimo.gshell.remote.message.ConnectMessage;
import org.apache.geronimo.gshell.remote.server.RshServer;
import org.apache.geronimo.gshell.remote.server.timeout.TimeoutManager;
import org.apache.mina.common.IoSession;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
@Component(role=ServerMessageHandler.class, hint="connect")
public class ConnectHandler
    extends ServerMessageHandlerSupport<ConnectMessage>
{
    @Requirement
    private CryptoContext crypto;
    
    @Requirement
    private TimeoutManager timeoutManager;
    
    public ConnectHandler() {
        super(ConnectMessage.class);
    }

    public void handle(final IoSession session, final ServerSessionContext context, final ConnectMessage message) throws Exception {
        // Try to cancel the timeout task
        if (!timeoutManager.cancelTimeout(session)) {
            log.warn("Aborting handshake processing; timeout has triggered");
        }
        else {
            // Hold on to the client's public key
            context.pk = message.getPublicKey();

            // Reply to the client with some details about the connection
            ConnectMessage.Result reply = new ConnectMessage.Result(crypto.getPublicKey());
            reply.setCorrelationId(message.getId());
            session.write(reply);

            // Schedule a task to timeout the login process
            timeoutManager.scheduleTimeout(session, RshServer.AUTH_TIMEOUT, new Runnable() {
                public void run() {
                    log.error("Timeout waiting for login from: {}", session.getRemoteAddress());
                    session.close();
                }
            });
        }
    }
}
