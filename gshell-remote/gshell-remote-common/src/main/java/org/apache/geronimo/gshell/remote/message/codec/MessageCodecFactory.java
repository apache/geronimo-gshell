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

package org.apache.geronimo.gshell.remote.message.codec;

import org.apache.geronimo.gshell.remote.message.Message;
import org.apache.geronimo.gshell.remote.message.MessageType;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class MessageCodecFactory
    implements ProtocolCodecFactory
{
    //
    // FIXME: Put in some better support for versioning the wire format.  Probably have to refactor the marshalling bits to make that work.
    //
    
    public static final byte VERSION = 1;

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

    //
    // Encoder
    //

    public class Encoder
        implements ProtocolEncoder
    {
        private Logger log = LoggerFactory.getLogger(getClass());

        public void encode(final IoSession session, final Object message, final ProtocolEncoderOutput out) throws Exception {
            assert session != null;
            assert message != null;
            assert out != null;
            
            Message msg = (Message)message;

            ByteBuffer buff = ByteBuffer.allocate(256, false);
            buff.setAutoExpand(true);

            MagicNumber.write(buff);

            buff.put(VERSION);

            buff.putEnum(msg.getType());

            msg.writeExternal(buff);

            buff.flip();
            
            out.write(buff);
        }

        public void dispose(final IoSession session) throws Exception {
            assert session != null;
            
            // Nothing
        }
    }

    //
    // Decoder
    //

    public class Decoder
        extends CumulativeProtocolDecoder
    {
        private Logger log = LoggerFactory.getLogger(getClass());

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

            msg.readExternal(in);

            out.write(msg);

            return true;
        }
    }
}