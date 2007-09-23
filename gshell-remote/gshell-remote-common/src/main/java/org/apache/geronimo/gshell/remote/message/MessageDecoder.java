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

import org.apache.geronimo.gshell.remote.crypto.CryptoContext;
import org.apache.geronimo.gshell.remote.crypto.CryptoContextAware;
import org.apache.geronimo.gshell.remote.marshal.Marshaller;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoderFactory;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

/**
 * Decodes {@link Message} instances.
 *
 * @version $Rev$ $Date$
 */
public class MessageDecoder
    extends MessageCodecSupport
    implements org.apache.mina.filter.codec.demux.MessageDecoder
{
    public MessageDecoder(final CryptoContext crypto) {
        super(crypto);
    }

    public MessageDecoderResult decodable(final IoSession session, final ByteBuffer in) {
        assert session != null;
        assert in != null;

        if (in.remaining() < HEADER_SIZE) {
            return MessageDecoderResult.NEED_DATA;
        }

        try {
            readMagic(in);

            readVersion(in);

            MessageType type = Marshaller.readEnum(in, MessageType.class);

            if (type == null) {
                return MessageDecoderResult.NOT_OK;
            }
        }
        catch (Exception e) {
            return MessageDecoderResult.NOT_OK;
        }

        // Make sure we have all of the data we need
        int len = in.getInt();

        if (in.remaining() != len) {
            return MessageDecoderResult.NEED_DATA;
        }

        return MessageDecoderResult.OK;
    }

    public MessageDecoderResult decode(final IoSession session, final ByteBuffer in, final ProtocolDecoderOutput out) throws Exception {
        assert session != null;
        assert in != null;
        assert out != null;

        readMagic(in);

        readVersion(in);

        MessageType type = Marshaller.readEnum(in, MessageType.class);

        Message msg = MessageType.create(type);

        if (msg instanceof CryptoContextAware) {
            ((CryptoContextAware)msg).setCryptoContext(crypto);
        }

        int len = in.getInt();

        log.trace("Decoding {} bytes", len);

        msg.readExternal(in);

        log.trace("Decoded: {}", msg);

        out.write(msg);

        return MessageDecoderResult.OK;
    }

    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {}

    /**
     * Produces {@link MessageDecoder} instances.
     */
    public static class Factory
        extends FactorySupport
        implements MessageDecoderFactory
    {
        public Factory(final CryptoContext crypto) {
            super(crypto);
        }

        public org.apache.mina.filter.codec.demux.MessageDecoder getDecoder() throws Exception {
            return new MessageDecoder(crypto);
        }
    }
}