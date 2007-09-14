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
import java.util.concurrent.atomic.AtomicLong;

import org.apache.mina.common.ByteBuffer;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public abstract class MessageSupport
    implements Message
{
    private static final AtomicLong ID_COUNTER = new AtomicLong(0);

    private long id = -1;

    private MessageType type;

    private transient Object attachment;

    protected MessageSupport(final MessageType type) {
        assert type != null;
        
        this.type = type;
    }
    
    public long getId() {
        synchronized (ID_COUNTER) {
            if (id == -1) {
                id = ID_COUNTER.incrementAndGet();
            }
        }
        
        return id;
    }

    public MessageType getType() throws IOException {
        return type;
    }

    public void setAttachment(final Object obj) {
        this.attachment = obj;
    }

    public Object getAttachment() {
        return attachment;
    }

    public void process(final MessageVisitor visitor) throws Exception {
        // Non-operation
    }

    //
    // MarshalAware
    //

    public void readExternal(final ByteBuffer buff) throws Exception {
        assert buff != null;

        id = buff.getLong();
        
        type = buff.getEnum(MessageType.class);
    }

    public void writeExternal(final ByteBuffer buff) throws Exception {
        assert buff != null;

        buff.putLong(id);
        
        buff.putEnum(type);
    }

    //
    // Marshal Helpers
    //

    private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");

    protected String readString(final ByteBuffer buff) throws CharacterCodingException {
        assert buff != null;

        return buff.getString(UTF_8_CHARSET.newDecoder());
    }

    protected void writeString(final ByteBuffer buff, final String str) throws CharacterCodingException {
        assert buff != null;
        assert str != null;

        buff.putString(str, UTF_8_CHARSET.newEncoder());
    }
}