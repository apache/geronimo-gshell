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

import java.util.concurrent.TimeUnit;

import org.apache.geronimo.gshell.common.tostring.ToStringBuilder;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class TimeValue
{
    public final long time;

    public final TimeUnit unit;

    public TimeValue(final long time, final TimeUnit unit) {
        this.time = time;
        this.unit = unit;
    }

    public long getTime() {
        return time;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("time", time)
                .append("unit", unit)
                .toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeValue timeValue = (TimeValue) o;

        if (time != timeValue.time) return false;

        return unit == timeValue.unit;
    }

    public int hashCode() {
        int result;

        result = (int) (time ^ (time >>> 32));
        result = 31 * result + (unit != null ? unit.hashCode() : 0);

        return result;
    }
}
