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

package org.apache.geronimo.gshell.whisper.stream;

import java.io.IOException;

import org.apache.geronimo.gshell.whisper.marshal.Marshaller;
import org.apache.geronimo.gshell.whisper.message.Message;
import org.apache.geronimo.gshell.whisper.message.MessageSupport;
import org.apache.geronimo.gshell.whisper.message.MessageType;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.RuntimeIOException;

/**
 * Write a buffer to a stream.
 *
 * @version $Rev$ $Date$
 */
public class StreamMessage
    extends MessageSupport
{
    private ByteBuffer buffer;

    public StreamMessage(final Type type, final ByteBuffer buffer) {
        super(type);

        if (buffer != null) {
            ByteBuffer tmp = ByteBuffer.allocate(buffer.remaining());

            tmp.put(buffer);
            tmp.flip();

            this.buffer = tmp;
        }
    }

    public StreamMessage(final ByteBuffer buffer) {
        this(Type.IN, buffer);
    }
    
    public StreamMessage() {
        this(null, null);
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(final ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public void readExternal(final ByteBuffer in) throws Exception {
        assert in != null;

        buffer = Marshaller.readBuffer(in);
    }

    public void writeExternal(final ByteBuffer out) throws Exception {
        assert out != null;

        Marshaller.writeBuffer(out, buffer);
    }

    public static enum Type
        implements MessageType
    {

        IN,  // (local SYSOUT to remote SYSIN)
        OUT, // ???
        ERR  // ???
        ;

        public Class<? extends Message> getType() {
            return StreamMessage.class;
        }

        // Dynamically fitgure out how bit we are, assumes each enum is sized the same
        private volatile Integer size;

        public int size() {
            if (size == null) {
                try {
                    size = Marshaller.marshall(IN).length;
                }
                catch (IOException e) {
                    throw new RuntimeIOException(e);
                }
            }

            return size;
        }
    }
}