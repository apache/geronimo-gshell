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
import org.apache.geronimo.gshell.remote.message.RshMessage;
import org.apache.geronimo.gshell.remote.server.timeout.TimeoutManager;
import org.apache.mina.common.IoSession;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
@Component(role=ServerMessageHandler.class, hint="login")
public class LoginHandler
    extends ServerMessageHandlerSupport<LoginMessage>
{
    @Requirement
    private TimeoutManager timeoutManager;

    public LoginHandler() {
        super(RshMessage.Type.LOGIN);
    }
    
    public void handle(final IoSession session, final ServerSessionContext context, final LoginMessage message) throws Exception {
        // Try to cancel the timeout task
        if (!timeoutManager.cancelTimeout(session)) {
            log.warn("Aborting login processing; timeout has triggered");
        }
        else {
            String username = message.getUsername();
            String password = message.getPassword();

            //
            // TODO: Add JAAC Crapo here...
            //

            // Remember our username
            context.username = username;

            log.info("Successfull authentication for user: {}, at location: {}", username, session.getRemoteAddress());

            LoginMessage.Success reply = new LoginMessage.Success();
            reply.setCorrelationId(message.getId());
            session.write(reply);
        }
    }
}
