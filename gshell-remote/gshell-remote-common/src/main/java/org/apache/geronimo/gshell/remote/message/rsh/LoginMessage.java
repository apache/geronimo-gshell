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

package org.apache.geronimo.gshell.remote.message.rsh;

import java.security.PublicKey;

import org.apache.geronimo.gshell.remote.marshall.Marshaller;
import org.apache.geronimo.gshell.remote.message.CryptoAwareMessageSupport;
import org.apache.geronimo.gshell.remote.message.MessageSupport;
import org.apache.geronimo.gshell.remote.message.MessageType;
import org.apache.geronimo.gshell.common.tostring.ToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.mina.common.ByteBuffer;
import org.codehaus.plexus.util.StringUtils;

//
// NOTE: This message does not support MessageListener, actually should never make it to a message listener anyways
//       since this is consumed by the security filter.
//

/**
 * Clients request to login to the server.
 *
 * @version $Rev$ $Date$
 */
public class LoginMessage
    extends CryptoAwareMessageSupport
{
    private PublicKey serverKey;

    private String username;

    private String password;

    public LoginMessage(final PublicKey serverKey, final String username, final String password) {
        super(MessageType.LOGIN);

        this.serverKey = serverKey;

        this.username = username;
        
        this.password = password;
    }

    public LoginMessage() {
        this(null, null, null);
    }

    public String toString() {
        return createToStringBuilder()
                .append("username", username)
                .append("password", StringUtils.repeat("*", password.length()))
                .toString();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

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

    /**
     * Response for login messages which were sucessful.
     */
    public static class Success
        extends MessageSupport
    {
        public Success() {
            super(MessageType.LOGIN_SUCCESS);
        }
    }

    /**
     * Response for login messages which have failed.
     */
    public static class Failure
        extends MessageSupport
    {
        private String reason;

        public Failure(final String reason) {
            super(MessageType.LOGIN_FAILURE);

            this.reason = reason;
        }

        public Failure() {
            this(null);
        }
        
        public String getReason() {
            return reason;
        }

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
    }
}