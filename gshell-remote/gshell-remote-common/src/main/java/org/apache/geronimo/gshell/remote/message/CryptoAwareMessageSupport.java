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

import java.security.Key;

import org.apache.geronimo.gshell.remote.codec.MarshallingUtil;
import org.apache.geronimo.gshell.remote.crypto.CryptoContext;
import org.apache.geronimo.gshell.remote.crypto.CryptoContextAware;
import org.apache.mina.common.ByteBuffer;

/**
 * Support for messages are aware of the {@link CryptoContext}.
 *
 * @version $Rev$ $Date$
 */
public abstract class CryptoAwareMessageSupport
    extends MessageSupport
    implements CryptoContextAware
{
    private transient CryptoContext ctx;

    public CryptoAwareMessageSupport(final MessageType type) {
        super(type);
    }

    public void setCryptoContext(final CryptoContext ctx) {
        assert ctx != null;
        
        this.ctx = ctx;
    }

    protected CryptoContext getCryptoContext() {
        if (ctx == null) {
            throw new IllegalStateException("Crypto context is not set");
        }
        
        return ctx;
    }

    protected String decryptString(final ByteBuffer in) throws Exception {
        assert in != null;

        byte[] bytes = MarshallingUtil.readBytes(in);
        
        if (bytes == null) {
            return null;
        }

        bytes = getCryptoContext().decrypt(bytes);

        return new String(bytes);
    }

    protected void encryptString(final ByteBuffer out, final Key key, final String str) throws Exception {
        assert out != null;
        assert key != null;

        byte[] bytes = null;

        if (str != null) {
            bytes = getCryptoContext().encrypt(key, str.getBytes());
        }

        MarshallingUtil.writeBytes(out, bytes);
    }

    protected void encryptString(final ByteBuffer out, final String str) throws Exception {
        encryptString(out, getCryptoContext().getPublicKey(), str);
    }
}