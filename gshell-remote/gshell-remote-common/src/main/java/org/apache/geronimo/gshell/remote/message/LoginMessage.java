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

import java.io.Serializable;

import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;

/**
 * Contains the user authentication details which the client will pass to the server after the
 * authetication of the connection has been established.
 *
 * @version $Rev$ $Date$
 */
public class LoginMessage
    extends RshMessage
{
    private String username;

    private char[] password;

    private String realm;
    
    public LoginMessage(final String username, final char[] password, final String realm) {
        super(Type.LOGIN);

        this.username = username;
        
        this.password = password;

        this.realm = realm;
    }

    public LoginMessage(final String username, final char[] password) {
        this(username, password, null);
    }

    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .setExcludeFieldNames(new String[] { "password" }).toString();
    }
    
    public String getUsername() {
        return username;
    }

    public char[] getPassword() {
        return password;
    }

    public String getRealm() {
        return realm;
    }

    public static class Success
        extends RshMessage
    {
        private Serializable token;

        public Success(Serializable token) {
            super(Type.LOGIN_SUCCESS);

            this.token = token;
        }

        public Serializable getToken() {
            return token;
        }
    }

    public static class Failure
        extends RshMessage
    {
        private String reason;

        public Failure(final String reason) {
            super(Type.LOGIN_FAILURE);

            this.reason = reason;
        }

        public Failure() {
            this(null);
        }
        
        public String getReason() {
            return reason;
        }
    }
}