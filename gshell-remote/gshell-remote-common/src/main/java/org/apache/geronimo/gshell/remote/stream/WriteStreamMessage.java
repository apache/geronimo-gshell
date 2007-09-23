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

package org.apache.geronimo.gshell.remote.stream;

import org.apache.geronimo.gshell.remote.marshal.Marshaller;
import org.apache.geronimo.gshell.remote.message.MessageSupport;
import org.apache.geronimo.gshell.remote.message.MessageType;
import org.apache.mina.common.ByteBuffer;

/**
 * Write a buffer to a stream.
 *
 * @version $Rev$ $Date$
 */
public class WriteStreamMessage
    extends MessageSupport
{
    private ByteBuffer buffer;

    public WriteStreamMessage(final ByteBuffer buffer) {
        super(MessageType.WRITE_STREAM);

        if (buffer != null) {
            ByteBuffer tmp = ByteBuffer.allocate(buffer.remaining());

            tmp.put(buffer);
            tmp.flip();

            this.buffer = tmp;
        }
    }

    public WriteStreamMessage() {
        this(null);
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(final ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public void readExternal(final ByteBuffer in) throws Exception {
        assert in != null;

        super.readExternal(in);

        buffer = Marshaller.readBuffer(in);
    }

    public void writeExternal(final ByteBuffer out) throws Exception {
        assert out != null;

        super.writeExternal(out);

        Marshaller.writeBuffer(out, buffer);
    }
}