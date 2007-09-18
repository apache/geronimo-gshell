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

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;

/**
 * ???
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

    //
    // MarshalAware
    //

    public void readExternal(final ByteBuffer buff) throws Exception {
        assert buff != null;

        id = readUuid(buff);

        correlationId = readUuid(buff);

        timestamp = buff.getLong();

        sequence = buff.getLong();
    }

    public void writeExternal(final ByteBuffer buff) throws Exception {
        assert buff != null;

        writeUuid(buff, id);

        writeUuid(buff, correlationId);

        buff.putLong(timestamp);

        sequence = SEQUENCE_COUNTER.getAndIncrement();
        
        buff.putLong(sequence);
    }

    //
    // Marshal Helpers
    //

    private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");

    protected String readString(final ByteBuffer in) throws CharacterCodingException {
        assert in != null;

        int len = in.getInt();

        return in.getString(len, UTF_8_CHARSET.newDecoder());
    }

    protected void writeString(final ByteBuffer out, final String str) throws CharacterCodingException {
        assert out != null;
        assert str != null;

        int len = str.length();
        out.putInt(len);
        
        out.putString(str, len, UTF_8_CHARSET.newEncoder());
    }

    private void writeUuid(final ByteBuffer out, final UUID uuid) throws Exception {
        assert out != null;

        if (uuid == null) {
            out.put((byte)0);
        }
        else {
            out.put((byte)1);

            out.putLong(uuid.getMostSignificantBits());
            out.putLong(uuid.getLeastSignificantBits());
        }
    }

    private UUID readUuid(final ByteBuffer in) throws Exception {
        assert in != null;

        byte isnull = in.get();

        if (isnull == 1) { // not null
            long msb = in.getLong();
            long lsb = in.getLong();
            return new UUID(msb, lsb);
        }
        else {
            return null;
        }
    }

    //
    // Reply Helpers
    //
    
    public WriteFuture reply(final MessageSupport msg) {
        assert msg != null;

        IoSession session = getSession();

        msg.setCorrelationId(getId());
        msg.freeze();
        
        return session.write(msg);
    }
}