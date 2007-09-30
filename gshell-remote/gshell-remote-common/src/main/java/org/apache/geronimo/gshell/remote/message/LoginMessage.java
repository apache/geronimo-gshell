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

import java.security.PublicKey;
import java.io.Serializable;

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

    private String password;

    public LoginMessage(final String username, final String password) {
        super(Type.LOGIN);

        this.username = username;
        
        this.password = password;
    }

    /*
    public String toString() {
        return createToStringBuilder()
                .append("username", username)
                .append("password", StringUtils.repeat("*", password.length()))
                .toString();
    }
    */

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    /*
    public void readExternal(final ByteBuffer in) throws Exception {
        assert in != null;

        super.readExternal(in);

        username = decryptString(in);

        password = decryptString(in);
    }

    public void writeExternal(final ByteBuffer out) throws Exception {
        assert out != null;

        super.writeExternal(out);

        encryptString(out, serverKey, username);

        encryptString(out, serverKey, password);
    }
    */

    /**
     * Response for login messages which were sucessful.
     */
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

    /**
     * Response for login messages which have failed.
     */
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

        /*
        public void readExternal(final ByteBuffer in) throws Exception {
            assert in != null;

            super.readExternal(in);

            reason = Marshaller.readString(in);
        }

        public void writeExternal(final ByteBuffer out) throws Exception {
            assert out != null;

            super.writeExternal(out);

            Marshaller.writeString(out, reason);
        }
        */
    }
}