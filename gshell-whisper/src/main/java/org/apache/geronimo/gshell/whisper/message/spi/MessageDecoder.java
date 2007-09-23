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

package org.apache.geronimo.gshell.whisper.message.spi;

import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.whisper.marshal.Marshaller;
import org.apache.geronimo.gshell.whisper.message.Message;
import org.apache.geronimo.gshell.whisper.message.MessageType;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

/**
 * Decodes {@link Message} instances.
 *
 * @version $Rev$ $Date$
 */
public class MessageDecoder
    implements org.apache.mina.filter.codec.demux.MessageDecoder
{
    private final MessageProvider provider;

    public MessageDecoder(final MessageProvider provider) {
        this.provider = provider;
    }

    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }
    
    public MessageDecoderResult decodable(final IoSession session, final ByteBuffer in) {
        assert session != null;
        assert in != null;

        MessageHeader header = provider.getMessageHeader();

        if (in.remaining() < header.size()) {
            return MessageDecoderResult.NEED_DATA;
        }

        //
        // FIXME: Use the MessageMarshaller instancer here some how..., maybe even unmarshall in a try/catch would work for now
        //
        
        try {
            header.readExternal(in);

            MessageType type = (MessageType) Marshaller.readObject(in);

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

        MessageMarshaller marshaller = provider.getMessageMarshaller();

        Message msg = marshaller.unmarshal(in);

        out.write(msg);

        return MessageDecoderResult.OK;
    }

    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {}

    public static class Factory
        implements org.apache.mina.filter.codec.demux.MessageDecoderFactory
    {
        private MessageProvider provider;

        public Factory(final MessageProvider provider) {
            this.provider = provider;
        }

        public org.apache.mina.filter.codec.demux.MessageDecoder getDecoder() throws Exception {
            return new MessageDecoder(provider);
        }
    }
}