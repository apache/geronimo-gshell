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

import org.apache.geronimo.gshell.remote.message.CryptoAwareMessageSupport;
import org.apache.geronimo.gshell.remote.message.MessageSupport;
import org.apache.geronimo.gshell.remote.message.MessageType;
import org.apache.mina.common.ByteBuffer;

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
    private transient PublicKey serverKey;

    private String username;

    //
    // NOTE: Marked as transiet to prevent the ToStringBuilder from displaying its value.
    //
    
    private transient String password;

    public LoginMessage(final PublicKey serverKey, final String username, final String password) {
        super(MessageType.LOGIN);

        this.serverKey = serverKey;

        this.username = username;
        
        this.password = password;
    }

    public LoginMessage() {
        this(null, null, null);
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
     * Server to client message to indicate successfull login.
     */
    public static class Result
        extends MessageSupport
    {
        public Result() {
            super(MessageType.LOGIN_RESULT);
        }
    }
}