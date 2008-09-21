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

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.annotation.PostConstruct;

import org.apache.geronimo.gshell.remote.jaas.Identity;
import org.apache.geronimo.gshell.remote.jaas.JaasConfigurationHelper;
import org.apache.geronimo.gshell.remote.jaas.UsernamePasswordCallbackHandler;
import org.apache.geronimo.gshell.remote.message.LoginMessage;
import org.apache.geronimo.gshell.remote.server.timeout.TimeoutManager;
import org.apache.geronimo.gshell.whisper.transport.Session;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Server handler for {@link LoginMessage} messages.
 *
 * @version $Rev$ $Date$
 */
public class LoginHandler
    extends ServerMessageHandlerSupport<LoginMessage>
{
    @Autowired
    private TimeoutManager timeoutManager;

    private String defaultRealm = "BogusLogin";

    public LoginHandler() {
        super(LoginMessage.class);
    }

    public String getDefaultRealm() {
        return defaultRealm;
    }

    public void setDefaultRealm(final String defaultRealm) {
        assert defaultRealm != null;
        
        this.defaultRealm = defaultRealm;
    }

    @PostConstruct
    public void init() {
        new JaasConfigurationHelper("server.login.conf").init();
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
            String realm = message.getRealm();
            if (realm == null) {
                realm = defaultRealm;
            }

            String username = message.getUsername();
            char[] password = message.getPassword();

            try {
                LoginContext loginContext = new LoginContext(realm, new UsernamePasswordCallbackHandler(username, password));
                loginContext.login();

                Subject subject = loginContext.getSubject();
                context.identity = new Identity(subject);

                log.debug("Username: {}, Identity: {}", context.getUsername(), context.identity);

                LoginMessage.Success reply = new LoginMessage.Success(context.identity.getToken());
                reply.setCorrelationId(message.getId());
                session.send(reply);
            }
            catch (LoginException e) {
                String reason = e.toString();
                log.debug("Login failed for user: {}, cause: {}", username, reason);

                LoginMessage.Failure reply = new LoginMessage.Failure(reason);
                reply.setCorrelationId(message.getId());
                session.send(reply);
            }
        }
    }

}
