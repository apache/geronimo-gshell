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

import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

import org.apache.geronimo.gshell.remote.crypto.CryptoContext;
import org.apache.geronimo.gshell.remote.marshal.Marshaller;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.filter.codec.ProtocolCodecException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for {@link Message} encoders and decoders.
 *
 * @version $Rev$ $Date$
 */
public class MessageCodecSupport
{
    protected static final byte[] MAGIC = { 'g', 's', 'h', 0 };

    protected static final byte VERSION = 1;

    /**
     * The size of the message header, which is made up of:
     *
     * <ol>
     * <li>MAGIC (4 bytes)</li>
     * <li>VERSION (1 byte)</li>
     * <li>MESSAGE TYPE (1 byte: enum)</li>
     * <li>BODY LENGTH (4 bytes: int)</li>
     * </ol>
     */
    protected static final int HEADER_SIZE = MAGIC.length + 1 + 1 + 4;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final CryptoContext crypto;

    protected MessageCodecSupport(final CryptoContext crypto) {
        assert crypto != null;

        this.crypto = crypto;
    }

    protected void writeMagic(final ByteBuffer out) throws Exception {
        assert out != null;

        out.put(MAGIC);
    }

    protected byte[] readMagic(final ByteBuffer in) throws Exception {
        assert in != null;

        byte[] bytes = new byte[MAGIC.length];

        in.get(bytes);

        if (!Arrays.equals(MAGIC, bytes)) {
            throw new ProtocolCodecException("Invalid MAGIC");
        }

        return bytes;
    }

    protected void writeVersion(final ByteBuffer out) throws Exception {
        assert out != null;

        out.put(VERSION);
    }

    protected byte readVersion(final ByteBuffer in) throws Exception {
        assert in != null;

        byte version = in.get();

        if (VERSION != version) {
            throw new ProtocolCodecException("Invalid version");
        }

        return version;
    }

    protected byte[] marshal(final Message msg) throws Exception {
        assert msg != null;

        log.trace("Marshalling: {}", msg);

        ByteBuffer out = ByteBuffer.allocate(256, false);
        out.setAutoExpand(true);

        writeMagic(out);

        writeVersion(out);

        Marshaller.writeEnum(out, msg.getType());

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

        log.trace("Marshalled size: {} bytes", bytes.length);

        return bytes;
    }

    /**
     * Support for {@link Message} encoder and decoder factories.
     */
    protected static class FactorySupport
    {
        protected final CryptoContext crypto;

        protected FactorySupport(final CryptoContext crypto) {
            assert crypto != null;

            this.crypto = crypto;
        }
    }
}