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

package org.apache.geronimo.gshell.remote.client.auth;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.geronimo.gshell.remote.message.LoginMessage;
import org.apache.geronimo.gshell.whisper.message.Message;
import org.apache.geronimo.gshell.whisper.transport.Session;
import org.apache.geronimo.gshell.whisper.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class RemoteLoginModule
    implements LoginModule
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Subject subject;

    private CallbackHandler callbackHandler;

    private String username;

    private Object clientIdentity;

    private Principal principal;

    public void initialize(final Subject subject, final CallbackHandler callbackHandler, final Map<String, ?> sharedState, final Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    private void reset() {
        username = null;
        clientIdentity = null;
        principal = null;
    }

    public boolean login() throws LoginException {
        // Get a handle on our transport
        Transport transport = getTransport();
        Session session = transport.getSession();

        // Process the username + password callbacks
        Callback[] callbacks = {
            new NameCallback("Username: "),
            new PasswordCallback("Password: ", false)
        };

        try {
            callbackHandler.handle(callbacks);
        }
        catch (IOException e) {
            throw new LoginException(e.getMessage());
        }
        catch (UnsupportedCallbackException e) {
            throw new LoginException(e.getMessage());
        }

        username = ((NameCallback)callbacks[0]).getName();
        char[] password = ((PasswordCallback) callbacks[1]).getPassword();

        // Send the login message
        Message response;
        try {
            response = session.request(new LoginMessage(username, password));
        }
        catch (Exception e) {
            throw new LoginException(e.getMessage());
        }

        if (response instanceof LoginMessage.Success) {
            clientIdentity = ((LoginMessage.Success)response).getToken();

            log.debug("Client identity: {}", clientIdentity);
        }
        else if (response instanceof LoginMessage.Failure) {
            LoginMessage.Failure failure = (LoginMessage.Failure)response;

            throw new LoginException("Login failed: " + failure.getReason());
        }
        
        return true;
    }

    public boolean commit() throws LoginException {
        principal = new ClientPrincipal(username, clientIdentity);

        log.debug("Principal: {}", principal);

        subject.getPrincipals().add(principal);

        return true;
    }

    public boolean abort() throws LoginException {
        reset();

        return true;
    }

    public boolean logout() throws LoginException {
        subject.getPrincipals().remove(principal);

        return true;
    }

    //
    // HACK: Transport access
    //
    
    private static final ThreadLocal<Transport> transportHolder = new ThreadLocal<Transport>();

    public static void setTransport(final Transport transport) {
        assert transport != null;

        transportHolder.set(transport);
    }

    public static void unsetTransport() {
        transportHolder.remove();
    }

    private static Transport getTransport() {
        Transport transport = transportHolder.get();

        if (transport == null) {
            throw new IllegalStateException("Transport has not been bound to the executing thread");    
        }

        return transport;
    }
}
