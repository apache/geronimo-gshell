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

package org.apache.geronimo.gshell.remote.server.auth;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.geronimo.gshell.remote.jaas.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class BogusLoginModule
    implements LoginModule
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Subject subject;

    private CallbackHandler callbackHandler;

    private Set<UserPrincipal> principals = new HashSet<UserPrincipal>();

    private String username;

    public void initialize(final Subject subject, final CallbackHandler callbackHandler, final Map<String, ?> sharedState, final Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    private void reset() {
        username = null;
    }

    public boolean login() throws LoginException {
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
        char[] passwd = ((PasswordCallback) callbacks[1]).getPassword();

        if ("bogus".equals(username)) {
            throw new FailedLoginException("Invalid username: " + username);
        }
        else if ("bogus".equals(new String(passwd))) {
            throw new FailedLoginException("Invalid password");
        }

        return true;
    }

    public boolean commit() throws LoginException {
        principals.add(new UserPrincipal(username));

        subject.getPrincipals().addAll(principals);

        reset();

        return true;
    }

    public boolean abort() throws LoginException {
        reset();

        return true;
    }

    public boolean logout() throws LoginException {
        subject.getPrincipals().removeAll(principals);

        principals.clear();

        reset();

        return true;
    }
}