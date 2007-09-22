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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.remote.codec.MarshallingUtil;
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
    private static final IDGenerator ID_GENERATOR = new LongIDGenerator();

    private static final AtomicLong SEQUENCE_COUNTER = new AtomicLong(0);

    private MessageType type;

    private ID id;

    private ID correlationId;

    private Long sequence;

    private long timestamp;
    
    private transient IoSession session;

    private transient boolean frozen;

    protected MessageSupport(final MessageType type) {
        assert type != null;
        
        this.type = type;

        this.id = ID_GENERATOR.generate();
        
        this.timestamp = System.currentTimeMillis();
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public MessageType getType() throws IOException {
        return type;
    }

    public ID getId() {
        return id;
    }
    
    public ID getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(final ID id) {
        ensureWritable();

        assert id != null;

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
    // Externalization
    //

    public void readExternal(final ByteBuffer in) throws Exception {
        assert in != null;

        id = (ID) MarshallingUtil.readObject(in);

        correlationId = (ID) MarshallingUtil.readObject(in);

        timestamp = in.getLong();

        sequence = in.getLong();
    }

    public void writeExternal(final ByteBuffer out) throws Exception {
        assert out != null;

        MarshallingUtil.writeObject(out, id);

        MarshallingUtil.writeObject(out, correlationId);

        out.putLong(timestamp);

        sequence = SEQUENCE_COUNTER.getAndIncrement();
        
        out.putLong(sequence);
    }

    //
    // LongID Generator
    //

    private static class LongIDGenerator
        implements IDGenerator
    {
        private static final AtomicLong ID_COUNTER = new AtomicLong(0);

        public ID generate() {
            return new LongID(ID_COUNTER.getAndIncrement());
        }
    }

    //
    // Long ID
    //

    private static class LongID
        implements ID
    {
        private final Long value;

        public LongID(final long value) {
            this.value = value;
        }

        public int hashCode() {
            return value.hashCode();
        }

        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            else if (obj == null) {
                return false;
            }
            else if (!(obj instanceof LongID)) {
                return false;
            }

            return value.equals(((LongID)obj).value);
        }

        public String toString() {
            return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    //
    // UUID Generator
    //

    private static class UUIDGenerator
        implements IDGenerator
    {
        public ID generate() {
            return new UUIDAdapter(UUID.randomUUID());
        }
    }

    //
    // UUID Adapter
    //

    private static class UUIDAdapter
        implements ID
    {
        private final UUID value;

        public UUIDAdapter(final UUID value) {
            this.value = value;
        }

        public int hashCode() {
            return value.hashCode();
        }

        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            else if (obj == null) {
                return false;
            }
            else if (!(obj instanceof UUIDAdapter)) {
                return false;
            }

            return value.equals(((UUIDAdapter)obj).value);
        }

        public String toString() {
            return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}