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

package org.apache.geronimo.gshell.whisper.message.spi;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.whisper.message.MessageID;
import org.apache.mina.common.ByteBuffer;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class LongMessageID
    implements MessageID
{
    private Long value;

    public LongMessageID(final long value) {
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
        else if (!(obj instanceof LongMessageID)) {
            return false;
        }

        return value.equals(((LongMessageID)obj).value);
    }

    public String toString() {
        return String.valueOf(value);
    }

    public void writeExternal(final ByteBuffer out) throws Exception {
        out.putLong(value);
    }

    public void readExternal(final ByteBuffer in) throws Exception {
        value = in.getLong();
    }

    public static class Factory
        implements MessageIDFactory
    {
        private static final AtomicLong COUNTER = new AtomicLong(0);
        
        public MessageID create() {
            return new LongMessageID(COUNTER.getAndIncrement());
        }

        public String toString() {
            return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
        }
    }
}
