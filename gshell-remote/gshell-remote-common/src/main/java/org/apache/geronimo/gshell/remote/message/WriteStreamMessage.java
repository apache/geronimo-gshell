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

import org.apache.mina.common.ByteBuffer;

/**
 * Write a buffer to a stream.
 *
 * @version $Rev$ $Date$
 */
public class WriteStreamMessage
    extends MessageSupport
{
    private String name;

    private ByteBuffer buffer;

    public WriteStreamMessage(final String name, final ByteBuffer buffer) {
        super(MessageType.WRITE_STREAM);

        this.name = name;

        if (buffer != null) {
            ByteBuffer tmp = ByteBuffer.allocate(buffer.remaining());

            tmp.put(buffer);
            tmp.flip();

            this.buffer = tmp;
        }
    }

    public WriteStreamMessage() {
        this(null, null);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
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

        name = readString(in);

        buffer = readBuffer(in);
    }

    public void writeExternal(final ByteBuffer out) throws Exception {
        assert out != null;

        super.writeExternal(out);

        writeString(out, name);

        writeBuffer(out, buffer);
    }

    public void process(final MessageVisitor visitor) throws Exception {
        assert visitor != null;

        visitor.visitWriteStream(this);
    }
}