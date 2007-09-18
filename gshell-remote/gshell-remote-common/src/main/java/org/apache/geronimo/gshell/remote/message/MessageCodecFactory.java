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
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides encoding and decoding support for {@link Message} instances.
 *
 * @version $Rev$ $Date$
 */
@Component(role=MessageCodecFactory.class)
public class MessageCodecFactory
    implements ProtocolCodecFactory
{
    //
    // FIXME: Put in some better support for versioning the wire format.  Probably have to refactor the marshalling bits to make that work.
    //
    
    public static final byte VERSION = 1;

    private Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private CryptoContext crypto;
    
    private final Encoder encoder;

    private final Decoder decoder;

    public MessageCodecFactory() {
        encoder = new Encoder();
        decoder = new Decoder();
    }

    public ProtocolEncoder getEncoder() throws Exception {
        return encoder;
    }

    public ProtocolDecoder getDecoder() throws Exception {
        return decoder;
    }

    private void setCryptoContext(final Message msg) {
        // We need to do a little bit of extra fluff to hook up support for encrypted messages
        if (msg instanceof CryptoContextAware) {
            log.trace("Attaching crypto context to: {}", msg);

            ((CryptoContextAware)msg).setCryptoContext(crypto);
        }
    }
    
    //
    // Encoder
    //

    public class Encoder
        implements ProtocolEncoder
    {
        public void encode(final IoSession session, final Object message, final ProtocolEncoderOutput out) throws Exception {
            assert session != null;
            assert message != null;
            assert out != null;
            
            Message msg = (Message)message;

            setCryptoContext(msg);

            log.trace("Serializing: {}", msg);

            ByteBuffer buff = ByteBuffer.allocate(256, false);
            buff.setAutoExpand(true);

            MagicNumber.write(buff);

            buff.put(VERSION);

            buff.putEnum(msg.getType());

            msg.writeExternal(buff);

            buff.flip();

            out.write(buff);
        }

        public void dispose(final IoSession session) throws Exception {}
    }

    //
    // Decoder
    //

    public class Decoder
        extends CumulativeProtocolDecoder
    {
        protected boolean doDecode(final IoSession session, final ByteBuffer in, final ProtocolDecoderOutput out) throws Exception {
            assert session != null;
            assert in != null;
            assert out != null;

            MagicNumber.read(in);

            byte version = in.get();

            if (version != VERSION) {
                throw new ProtocolDecoderException("Invalid version: " + version);
            }

            MessageType type = in.getEnum(MessageType.class);

            Message msg = MessageType.create(type);

            setCryptoContext(msg);

            msg.readExternal(in);

            log.trace("Deserialized: {}", msg);

            out.write(msg);

            return true;
        }
    }
}