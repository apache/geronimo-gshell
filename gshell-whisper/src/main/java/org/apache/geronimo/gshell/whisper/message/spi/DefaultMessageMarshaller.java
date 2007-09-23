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

import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.whisper.crypto.CryptoContextAware;
import org.apache.geronimo.gshell.whisper.marshal.Marshaller;
import org.apache.geronimo.gshell.whisper.message.Message;
import org.apache.geronimo.gshell.whisper.message.MessageType;
import org.apache.mina.common.ByteBuffer;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class DefaultMessageMarshaller
    implements MessageMarshaller
{
    private final MessageProvider provider;

    public DefaultMessageMarshaller(final MessageProvider provider) {
        this.provider = provider;
    }

    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }
    
    public void marshal(final ByteBuffer out, final Message msg) throws Exception {
        MessageHeader header = provider.getMessageHeader();

        header.writeExternal(out);

        Marshaller.writeObject(out, msg.getType());

        // Determine the length of the message body
        out.mark();
        out.putInt(0);
        msg.writeExternal(out);

        int bodyStart = header.size();
        int pos = out.position();
        int len = pos - bodyStart;

        out.reset();

        // Write the length of the body
        out.putInt(len);
        out.position(pos);
        out.limit(pos);
        out.flip();
    }

    public byte[] marshal(final Message msg) throws Exception {
        ByteBuffer out = ByteBuffer.allocate(256, false);
        out.setAutoExpand(true);

        marshal(out, msg);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        WritableByteChannel channel = Channels.newChannel(baos);
        channel.write(out.buf());
        channel.close();

        return baos.toByteArray();
    }

    public Message unmarshal(final ByteBuffer in) throws Exception {
        MessageHeader header = provider.getMessageHeader();

        header.readExternal(in);

        MessageType type = (MessageType) Marshaller.readObject(in);

        Message msg = provider.getMessageFactory().create(type);

        //
        // FIXME: This could be handled by the factory...
        //

        if (msg instanceof CryptoContextAware) {
            ((CryptoContextAware)msg).setCryptoContext(provider.getCryptoContext());
        }

        int len = in.getInt(); // ignored for now

        msg.readExternal(in);

        return msg;
    }
}