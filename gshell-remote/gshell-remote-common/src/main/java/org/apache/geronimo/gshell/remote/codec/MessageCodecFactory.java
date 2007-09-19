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

package org.apache.geronimo.gshell.remote.codec;

import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Set;
import java.util.Arrays;

import org.apache.geronimo.gshell.remote.message.Message;
import org.apache.geronimo.gshell.remote.message.MessageType;
import org.apache.geronimo.gshell.remote.crypto.CryptoContext;
import org.apache.geronimo.gshell.remote.crypto.CryptoContextAware;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.ProtocolCodecException;
import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;
import org.apache.mina.filter.codec.demux.MessageDecoderAdapter;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;
import org.apache.mina.filter.codec.demux.MessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.annotations.Component;

/**
 * Provides encoding and decoding support for {@link Message} instances.
 *
 * @version $Rev$ $Date$
 */
@Component(role=MessageCodecFactory.class)
public class MessageCodecFactory
    extends DemuxingProtocolCodecFactory
{
    private static final byte[] MAGIC = { 'g', 's', 'h', 0 };

    private static final byte VERSION = 1;

    private static final int HEADER_SIZE =
            MAGIC.length +
            1 + // version (byte)
            1 + // message type (enum byte)
            4;  // body length (int)

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private CryptoContext crypto;

    public MessageCodecFactory() {
        register(new Decoder());

        //noinspection unchecked
        register(new Encoder());
    }

    private void attachCryptoContext(final Message msg) {
        // We need to do a little bit of extra fluff to hook up support for encrypted messages
        if (msg instanceof CryptoContextAware) {
            log.debug("Attaching crypto context to: {}", msg);

            ((CryptoContextAware)msg).setCryptoContext(crypto);
        }
    }

    private void writeMagic(final ByteBuffer out) throws Exception {
        assert out != null;

        out.put(MAGIC);
    }

    private byte[] readMagic(final ByteBuffer in) throws Exception {
        assert in != null;

        byte[] bytes = new byte[MAGIC.length];

        in.get(bytes);

        if (!Arrays.equals(MAGIC, bytes)) {
            throw new ProtocolCodecException("Invalid MAGIC");
        }

        return bytes;
    }

    private void writeVersion(final ByteBuffer out) throws Exception {
        assert out != null;

        out.put(VERSION);
    }

    private byte readVersion(final ByteBuffer in) throws Exception {
        assert in != null;

        byte version = in.get();

        if (VERSION != version) {
            throw new ProtocolCodecException("Invalid version");
        }

        return version;
    }

    private byte[] marshal(final Message msg) throws Exception {
        assert msg != null;

        log.debug("Marshalling: {}", msg);
        
        ByteBuffer out = ByteBuffer.allocate(256, false);
        out.setAutoExpand(true);

        writeMagic(out);

        writeVersion(out);

        out.putEnum(msg.getType());

        // Determine the length of the message body
        out.mark();
        out.putInt(0);
        msg.writeExternal(out);

        int bodyStart = HEADER_SIZE;
        int pos = out.position();
        int len = pos - bodyStart;

        out.reset();
        
        // Write the length of the body
        out.putInt(len);
        out.position(pos);
        out.limit(pos);
        out.flip();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        WritableByteChannel channel = Channels.newChannel(baos);
        channel.write(out.buf());
        channel.close();

        byte[] bytes = baos.toByteArray();

        log.debug("Marshalled size: {} bytes", bytes.length);

        return bytes;
    }

    //
    // Encoder
    //

    public class Encoder
        implements MessageEncoder
    {
        public Set getMessageTypes() {
            return MessageType.types();
        }

        public void encode(final IoSession session, final Object message, final ProtocolEncoderOutput out) throws Exception {
            assert session != null;
            assert message != null;
            assert out != null;

            Message msg = (Message)message;

            log.debug("Encoding: {}", msg);

            attachCryptoContext(msg);

            byte[] bytes = marshal(msg);

            log.debug("Encoded {} bytes", bytes.length);

            out.write(ByteBuffer.wrap(bytes));
        }
    }

    //
    // Decoder
    //

    public class Decoder
        extends MessageDecoderAdapter
    {
        public MessageDecoderResult decodable(final IoSession session, final ByteBuffer in) {
            assert session != null;
            assert in != null;

            if (in.remaining() < HEADER_SIZE) {
                return MessageDecoderResult.NEED_DATA;
            }

            try {
                readMagic(in);

                readVersion(in);

                MessageType type = in.getEnum(MessageType.class);

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

            MessageType type = in.getEnum(MessageType.class);

            Message msg = MessageType.create(type);

            attachCryptoContext(msg);

            int len = in.getInt();

            log.debug("Decoding {} bytes", len);

            msg.readExternal(in);

            log.debug("Decoded: {}", msg);

            out.write(msg);

            return MessageDecoderResult.OK;
        }
    }
}