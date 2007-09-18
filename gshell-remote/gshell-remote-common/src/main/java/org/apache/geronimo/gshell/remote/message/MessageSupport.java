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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;

/**
 * Support for {@link Message} implementations.
 *
 * @version $Rev$ $Date$
 */
public abstract class MessageSupport
    implements Message
{
    private static final AtomicLong SEQUENCE_COUNTER = new AtomicLong(0);

    private transient MessageType type;

    private UUID id;

    private UUID correlationId;

    private Long sequence;

    private long timestamp;
    
    private transient IoSession session;

    private transient boolean frozen;

    protected MessageSupport(final MessageType type) {
        assert type != null;
        
        this.type = type;

        this.id = UUID.randomUUID();

        this.timestamp = System.currentTimeMillis();
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    public MessageType getType() throws IOException {
        return type;
    }

    public UUID getId() {
        return id;
    }
    
    public UUID getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(final UUID id) {
        assert id != null;
        
        ensureWritable();
        
        this.correlationId = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getSequence() {
        if (sequence == null) {
            throw new IllegalStateException("Sequence number is set upon write and is not yet available");
        }

        return sequence;
    }

    public void setSession(final IoSession session) {
        ensureWritable();
        
        this.session = session;
    }

    public IoSession getSession() {
        if (session == null) {
            throw new IllegalStateException("Session has not been attached");
        }
        
        return session;
    }

    protected void ensureWritable() {
        if (frozen) {
            throw new IllegalStateException("Message is frozen");
        }
    }

    public void freeze() {
        frozen = true;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void process(final MessageVisitor visitor) throws Exception {
        // Non-operation
    }

    public WriteFuture reply(final Message msg) {
        assert msg != null;

        IoSession session = getSession();

        msg.setCorrelationId(getId());
        msg.freeze();

        return session.write(msg);
    }
    
    //
    // Serialization
    //

    public void readExternal(final ByteBuffer in) throws Exception {
        assert in != null;

        id = readUuid(in);

        correlationId = readUuid(in);

        timestamp = in.getLong();

        sequence = in.getLong();
    }

    public void writeExternal(final ByteBuffer out) throws Exception {
        assert out != null;

        writeUuid(out, id);

        writeUuid(out, correlationId);

        out.putLong(timestamp);

        sequence = SEQUENCE_COUNTER.getAndIncrement();
        
        out.putLong(sequence);
    }

    //
    // Boolean Serialization
    //

    private static final byte TRUE = 1;

    private static final byte FALSE = 0;

    protected boolean readBoolean(final ByteBuffer in) {
        assert in != null;

        byte b = in.get();

        if (b == TRUE) {
            return true;
        }
        else if (b == FALSE) {
            return false;
        }
        else {
            throw new Error();
        }
    }

    protected void writeBoolean(final ByteBuffer out, final boolean bool) {
        assert out != null;

        if (bool) {
            out.put(TRUE);
        }
        else {
            out.put(FALSE);
        }
    }

    //
    // Byte[] Serialization
    //

    protected byte[] readBytes(final ByteBuffer in) {
        assert in != null;

        boolean isNull = readBoolean(in);

        if (isNull) {
            return null;
        }

        int len = in.getInt();

        byte[] bytes = new byte[len];

        in.get(bytes);

        return bytes;
    }

    protected void writeBytes(final ByteBuffer out, final byte[] bytes) {
        assert out != null;

        if (bytes == null) {
            writeBoolean(out, true);
        }
        else {
            writeBoolean(out, false);

            out.putInt(bytes.length);

            out.put(bytes);
        }
    }

    //
    // ByteBuffer Serialization
    //

    protected ByteBuffer readBuffer(final ByteBuffer in) {
        assert in != null;

        byte[] bytes = readBytes(in);

        if (bytes == null) {
            return null;
        }

        return ByteBuffer.wrap(bytes);
    }

    protected void writeBuffer(final ByteBuffer out, final ByteBuffer buffer) {
        assert out != null;

        if (buffer == null) {
            writeBytes(out, null);
        }
        else {
            writeBoolean(out, false);
            
            out.putInt(buffer.remaining());

            out.put(buffer);
        }
    }

    //
    // Object Serialization
    //

    protected Object readObject(final ByteBuffer in) throws IOException, ClassNotFoundException {
        assert in != null;

        byte[] bytes = readBytes(in);

        if (bytes == null) {
            return null;
        }
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);

        return ois.readObject();
    }

    protected void writeObject(final ByteBuffer out, final Object obj) throws IOException {
        assert out != null;

        byte[] bytes = null;

        if (obj != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            oos.writeObject(obj);
            oos.flush();

            bytes = baos.toByteArray();
        }

        writeBytes(out, bytes);
    }

    //
    // String Serialization
    //

    private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");

    protected String readString(final ByteBuffer in) throws CharacterCodingException {
        assert in != null;

        int len = in.getInt();

        if (len == -1) {
            return null;
        }

        return in.getString(len, UTF_8_CHARSET.newDecoder());
    }

    protected void writeString(final ByteBuffer out, final String str) throws CharacterCodingException {
        assert out != null;

        if (str == null) {
            out.putInt(-1);
        }
        else {
            int len = str.length();
            out.putInt(len);

            out.putString(str, len, UTF_8_CHARSET.newEncoder());
        }
    }

    //
    // UUID Serialization
    //

    protected UUID readUuid(final ByteBuffer in) throws Exception {
        assert in != null;

        boolean isNull = readBoolean(in);

        if (isNull) {
            return null;
        }

        long msb = in.getLong();
        
        long lsb = in.getLong();

        return new UUID(msb, lsb);
    }

    protected void writeUuid(final ByteBuffer out, final UUID uuid) throws Exception {
        assert out != null;

        if (uuid == null) {
            writeBoolean(out, true);
        }
        else {
            writeBoolean(out, false);

            out.putLong(uuid.getMostSignificantBits());
            
            out.putLong(uuid.getLeastSignificantBits());
        }
    }
}