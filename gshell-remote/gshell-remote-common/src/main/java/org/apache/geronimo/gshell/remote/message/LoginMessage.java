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

package org.apache.geronimo.gshell.remote.message;

import org.apache.geronimo.gshell.yarn.Yarn;
import org.apache.geronimo.gshell.yarn.YarnStyle;

import java.io.Serializable;

/**
 * Client to server message which contains the user authentication details which the client will pass to the server after the
 * authetication of the connection has been established.
 *
 * @version $Rev$ $Date$
 */
public class LoginMessage
    extends RshMessage
{
    private final String username;

    @YarnStyle(omit=true)
    private final String password;

    private final String realm;
    
    public LoginMessage(final String username, final String password, final String realm) {
        this.username = username;
        
        this.password = password;

        this.realm = realm;
    }

    public LoginMessage(final String username, final String password) {
        this(username, password, null);
    }

    public String toString() {
        return Yarn.render(this);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRealm() {
        return realm;
    }

    /**
     * Server to client message to indicate login success.
     */
    public static class Success
        extends RshMessage
    {
        private final Serializable token;

        public Success(Serializable token) {
            this.token = token;
        }

        public Serializable getToken() {
            return token;
        }
    }

    /**
     * Server to client message to indicate login failure.
     */
    public static class Failure
        extends RshMessage
    {
        private final String reason;

        public Failure(final String reason) {
            this.reason = reason;
        }

        public String getReason() {
            return reason;
        }
    }
}