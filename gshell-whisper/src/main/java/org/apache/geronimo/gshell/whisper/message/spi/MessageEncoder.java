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

import java.util.Set;

import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.whisper.crypto.CryptoContextAware;
import org.apache.geronimo.gshell.whisper.message.Message;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * Encodes {@link Message} instances.
 *
 * @version $Rev$ $Date$
 */
public class MessageEncoder
    implements org.apache.mina.filter.codec.demux.MessageEncoder
{
    private final MessageProvider provider;

    public MessageEncoder(final MessageProvider provider) {
        this.provider = provider;
    }

    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }

    public Set<Class<?>> getMessageTypes() {
        return provider.getMessageTypes();
    }

    public void encode(final IoSession session, final Object message, final ProtocolEncoderOutput out) throws Exception {
        assert session != null;
        assert message != null;
        assert out != null;

        Message msg = (Message)message;

        //
        // FIXME: This could be handled by the factory...
        //

        if (msg instanceof CryptoContextAware) {
            ((CryptoContextAware)msg).setCryptoContext(provider.getCryptoContext());
        }

        MessageMarshaller marshaller = provider.getMessageMarshaller();

        byte[] bytes = marshaller.marshal(msg);

        out.write(ByteBuffer.wrap(bytes));
    }

    public static class Factory
        implements org.apache.mina.filter.codec.demux.MessageEncoderFactory
    {
        private MessageProvider provider;

        public Factory(final MessageProvider provider) {
            this.provider = provider;
        }

        public org.apache.mina.filter.codec.demux.MessageEncoder getEncoder() throws Exception {
            return new MessageEncoder(provider);
        }
    }
}