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

import java.util.Set;

import org.apache.geronimo.gshell.remote.crypto.CryptoContext;
import org.apache.geronimo.gshell.remote.crypto.CryptoContextAware;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoderFactory;

/**
 * Encodes {@link Message} instances.
 *
 * @version $Rev$ $Date$
 */
public class MessageEncoder
    extends MessageCodecSupport
    implements org.apache.mina.filter.codec.demux.MessageEncoder
{
    public MessageEncoder(final CryptoContext crypto) {
        super(crypto);
    }

    public Set<Class<?>> getMessageTypes() {
        return MessageType.types();
    }

    public void encode(final IoSession session, final Object message, final ProtocolEncoderOutput out) throws Exception {
        assert session != null;
        assert message != null;
        assert out != null;

        Message msg = (Message)message;

        log.trace("Encoding: {}", msg);

        if (msg instanceof CryptoContextAware) {
            ((CryptoContextAware)msg).setCryptoContext(crypto);
        }

        byte[] bytes = marshal(msg);

        log.trace("Encoded {} bytes", bytes.length);

        out.write(ByteBuffer.wrap(bytes));
    }

    /**
     * Produces {@link MessageEncoder} instances.
     */
    public static class Factory
        extends FactorySupport
        implements MessageEncoderFactory
    {
        public Factory(final CryptoContext crypto) {
            super(crypto);
        }

        public org.apache.mina.filter.codec.demux.MessageEncoder getEncoder() throws Exception {
            return new MessageEncoder(crypto);
        }
    }
}