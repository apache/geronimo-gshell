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

package org.apache.geronimo.gshell.whisper.message;

import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.whisper.marshal.Marshaller;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;

/**
 * Support for {@link Message} implementations.
 *
 * @version $Rev$ $Date$
 */
public class MessageSupport
    implements Message
{
    private MessageType type;

    private MessageID id;

    private MessageID cid;

    private Long sequence;

    private long timestamp;
    
    private transient IoSession session;

    private transient boolean frozen;

    protected MessageSupport(final MessageType type) {
        assert type != null;
        
        this.type = type;

        this.timestamp = System.currentTimeMillis();
    }

    public int hashCode() {
        return getId().hashCode();
    }
    
    public MessageType getType() {
        return type;
    }

    public MessageID getId() {
        return id;
    }
    
    public MessageID getCorrelationId() {
        return cid;
    }

    public void setCorrelationId(final MessageID id) {
        ensureWritable();

        if (this.cid == null) {
            throw new IllegalStateException("Correlation ID has already been set");
        }

        this.cid = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getSequence() {
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

    public void process(final IoSession session, final MessageVisitor visitor) throws Exception {
        // Non-operation
    }

    public WriteFuture reply(final Message msg) {
        assert msg != null;

        IoSession session = getSession();

        msg.setCorrelationId(getId());
        msg.freeze();

        return session.write(msg);
    }

    public void readExternal(final ByteBuffer in) throws Exception {
        assert in != null;

        id = (MessageID) Marshaller.unmarshal(in);

        cid = (MessageID) Marshaller.unmarshal(in);

        timestamp = in.getLong();

        sequence = in.getLong();
    }

    public void writeExternal(final ByteBuffer out) throws Exception {
        assert out != null;

        Marshaller.marshal(out, getId());

        Marshaller.marshal(out, getCorrelationId());

        out.putLong(getTimestamp());

        out.putLong(getSequence());
    }

    //
    // ToString Muck
    //

    protected static final class MessageToStringStyle
        extends ToStringStyle
    {
        private static final long serialVersionUID = 1L;

        public static final String[] EXCLUDED_FIELDS = {
            "type",
            "session",
            "frozen"
        };

        MessageToStringStyle() {
            this.setUseShortClassName(true);
            this.setUseIdentityHashCode(false);
        }

        protected void appendClassName(StringBuffer buffer, Object object) {
            if (object instanceof Message) {
                Message msg = (Message) object;

                buffer.append(msg.getType());
            }
            else {
                super.appendClassName(buffer, object);
            }
        }

        private Object readResolve() {
            return MESSAGE_STYLE;
        }
    }

    private static final ToStringStyle MESSAGE_STYLE = new MessageToStringStyle();

    protected ToStringBuilder createToStringBuilder() {
        return new ToStringBuilder(this, MESSAGE_STYLE)
                .append("id", getId())
                .append("sequence", getSequence())
                .append("timestamp", getTimestamp());
    }

    public String toString() {
        return new ReflectionToStringBuilder(this, MESSAGE_STYLE)
                .setExcludeFieldNames(MessageToStringStyle.EXCLUDED_FIELDS)
                .toString();
    }
}