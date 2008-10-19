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

import org.apache.geronimo.gshell.remote.message.LoginMessage;
import org.apache.geronimo.gshell.remote.server.timeout.TimeoutManager;
import org.apache.geronimo.gshell.whisper.transport.Session;

import java.util.UUID;

/**
 * Server handler for {@link LoginMessage} messages.
 *
 * @version $Rev$ $Date$
 */
public class LoginHandler
    extends ServerMessageHandlerSupport<LoginMessage>
{
    private final TimeoutManager timeoutManager;

    public LoginHandler(final TimeoutManager timeoutManager) {
        super(LoginMessage.class);
        assert timeoutManager != null;
        this.timeoutManager = timeoutManager;
    }

    public void handle(final Session session, final ServerSessionContext context, final LoginMessage message) throws Exception {
        assert session != null;
        assert context != null;
        assert message != null;

        // Try to cancel the timeout task
        if (!timeoutManager.cancelTimeout(session)) {
            log.warn("Aborting login processing; timeout has triggered");
        }
        else {
            String username = message.getUsername();
            String password = message.getPassword();

            //
            // HACK: Just accept anything that is not "bogus"
            //

            log.debug("Processing login: username='{}', password='{}'", username, password);

            if (username == null || username.equals("bogus")) {
                String reason = "Invalid username";
                log.debug("Login failed for user: {}, cause: {}", username, reason);

                LoginMessage.Failure reply = new LoginMessage.Failure(reason);
                reply.setCorrelationId(message.getId());
                session.send(reply);
            }
            else if (password == null || password.equals("bogus")) {
                String reason = "Invalid password";
                log.debug("Login failed for user: {}, cause: {}", username, reason);

                LoginMessage.Failure reply = new LoginMessage.Failure(reason);
                reply.setCorrelationId(message.getId());
                session.send(reply);
            }
            else {
                UUID identity = UUID.randomUUID();
                LoginMessage.Success reply = new LoginMessage.Success(identity);
                reply.setCorrelationId(message.getId());
                session.send(reply);
            }
        }
    }

}
