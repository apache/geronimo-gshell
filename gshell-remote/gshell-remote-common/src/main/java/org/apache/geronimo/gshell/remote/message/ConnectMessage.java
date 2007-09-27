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
import java.util.UUID;

/**
 * Initial client to server message to initiate the connection.
 *
 * @version $Rev$ $Date$
 */
public class ConnectMessage
    extends RshMessage
{
    private PublicKey publicKey;

    protected ConnectMessage(final Type type, final PublicKey publicKey) {
        super(type);

        this.publicKey = publicKey;
    }

    public ConnectMessage(final PublicKey clientKey) {
        this(Type.CONNECT, clientKey);
    }

    public ConnectMessage() {
        this(null);
    }

    public PublicKey getPublicKey() {
        if (publicKey == null) {
            throw new IllegalStateException("Missing public key");
        }

        return publicKey;
    }

    public void setPublicKey(final PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    /*
    public void readExternal(final ByteBuffer in) throws Exception {
        assert in != null;

        super.readExternal(in);

        byte[] bytes = Marshaller.readBytes(in);
        
        if (bytes == null) {
            throw new IllegalStateException();
        }

        publicKey = getCryptoContext().deserializePublicKey(bytes);
    }

    public void writeExternal(final ByteBuffer out) throws Exception {
        assert out != null;

        super.writeExternal(out);

        Marshaller.writeBytes(out, getPublicKey().getEncoded());
    }
    */

    /**
     * Indicates the first part of the connection handshake was successful.
     */
    public static class Result
        extends ConnectMessage
    {
        private UUID clientId;

        public Result(final UUID clientId, final PublicKey serverKey) {
            super(Type.CONNECT_RESULT, serverKey);

            this.clientId = clientId;
        }

        public Result() {
            this(null, null);
        }

        public UUID getClientID() {
            return clientId;
        }

        /*
        public void readExternal(final ByteBuffer in) throws Exception {
            assert in != null;

            super.readExternal(in);

            clientId = Marshaller.readUuid(in);
        }

        public void writeExternal(final ByteBuffer out) throws Exception {
            assert out != null;

            super.writeExternal(out);

            Marshaller.writeUuid(out, clientId);
        }
        */
    }
}