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

package org.apache.geronimo.gshell.remote.util;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.apache.geronimo.gshell.common.tostring.ToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.remote.marshall.MarshalAware;
import org.apache.geronimo.gshell.remote.marshall.Marshaller;
import org.apache.mina.common.ByteBuffer;

/**
 * A representaion of a duration of time.
 * 
 * @version $Rev$ $Date$
 */
public class Duration
    implements MarshalAware, Serializable
{
    public static final TimeUnit DEFAULT_UNIT = TimeUnit.MILLISECONDS;

    private long value;

    private TimeUnit unit;

    public Duration(final long value, final TimeUnit unit) {
        this.value = value;

        if (unit == null) {
            this.unit = DEFAULT_UNIT;
        }
        else {
            this.unit = unit;
        }
    }

    public Duration(final long value) {
        this(value, DEFAULT_UNIT);
    }

    public long getValue() {
        return value;
    }

    public TimeUnit getUnit() {
        return unit;
    }
    
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("value", value)
                .append("unit", unit)
                .toString();
    }

    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Duration duration = (Duration) obj;

        return value == duration.value && unit == duration.unit;
    }

    public int hashCode() {
        int result;

        result = (int) (value ^ (value >>> 32));
        result = 31 * result + (unit != null ? unit.hashCode() : 0);

        return result;
    }

    public void writeExternal(final ByteBuffer out) throws Exception {
        assert out != null;
        
        out.putLong(value);

        Marshaller.writeEnum(out, unit);
    }

    public void readExternal(final ByteBuffer in) throws Exception {
        assert in != null;

        value = in.getLong();

        unit = Marshaller.readEnum(in, TimeUnit.class);

        assert unit != null;
    }
}
