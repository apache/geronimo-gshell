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
import org.apache.geronimo.gshell.remote.message.MessageType;
import org.apache.mina.common.ByteBuffer;

//
// NOTE: This message does not support MessageListener, actually should never make it to a message listener anyways
//       since this is consumed by the security filter.
//

/**
 * Initial client handshake which contains the clients public key.
 *
 * @version $Rev$ $Date$
 */
public class HandShakeMessage
    extends CryptoAwareMessageSupport
{
    private PublicKey clientKey;

    protected HandShakeMessage(final MessageType type, final PublicKey clientKey) {
        super(type);

        this.clientKey = clientKey;
    }

    public HandShakeMessage(final PublicKey clientKey) {
        this(MessageType.HANDSHAKE, clientKey);
    }

    public HandShakeMessage() {
        this(null);
    }

    public PublicKey getClientKey() {
        if (clientKey == null) {
            throw new IllegalStateException("Missing client key");
        }

        return clientKey;
    }

    public void setClientKey(final PublicKey clientKey) {
        this.clientKey = clientKey;
    }

    public void readExternal(final ByteBuffer in) throws Exception {
        assert in != null;

        super.readExternal(in);

        byte[] bytes = Marshaller.readBytes(in);
        
        if (bytes == null) {
            throw new IllegalStateException();
        }

        clientKey = getCryptoContext().deserializePublicKey(bytes);
    }

    public void writeExternal(final ByteBuffer out) throws Exception {
        assert out != null;

        super.writeExternal(out);

        Marshaller.writeBytes(out, getClientKey().getEncoded());
    }

    /**
     * Reply from server to client which contains the server's public key.
     */
    public static class Result
        extends HandShakeMessage
    {
        public Result(final PublicKey publicKey) {
            super(MessageType.HANDSHAKE_RESULT, publicKey);
        }

        public Result() {
            this(null);
        }
    }
}