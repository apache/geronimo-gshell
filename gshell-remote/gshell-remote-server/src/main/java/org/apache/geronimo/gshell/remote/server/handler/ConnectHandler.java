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
import org.apache.geronimo.gshell.whisper.transport.Session;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class ConnectHandler
    extends ServerMessageHandlerSupport<ConnectMessage>
{
    private CryptoContext crypto;
    
    private TimeoutManager timeoutManager;
    
    public ConnectHandler() {
        super(ConnectMessage.class);
    }

    public ConnectHandler(final CryptoContext crypto, final TimeoutManager timeoutManager) {
        this();
        this.crypto = crypto;
        this.timeoutManager = timeoutManager;
    }

    public void handle(final Session session, final ServerSessionContext context, final ConnectMessage message) throws Exception {
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
            session.send(reply);

            // Schedule a task to timeout the login process
            timeoutManager.scheduleTimeout(session, RshServer.AUTH_TIMEOUT, new Runnable() {
                public void run() {
                    log.error("Timeout waiting for login from: {}", session);
                    
                    session.close();
                }
            });
        }
    }
}
