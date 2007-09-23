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

import java.util.UUID;

import org.apache.geronimo.gshell.remote.marshal.Marshaller;
import org.apache.mina.common.ByteBuffer;

/**
 * Provides {@link Message.ID} instances based on {@link UUID} objects.
 *
 * @version $Rev$ $Date$
 */
public class UuidMessageID
    implements Message.ID
{
    private UUID value;

    public UuidMessageID(final UUID value) {
        this.value = value;
    }

    public UuidMessageID() {
        this(UUID.randomUUID());
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
        else if (!(obj instanceof UuidMessageID)) {
            return false;
        }

        return value.equals(((UuidMessageID)obj).value);
    }

    public String toString() {
        return String.valueOf(value);
    }

    public void writeExternal(final ByteBuffer out) throws Exception {
        Marshaller.writeUuid(out, value);
    }

    public void readExternal(final ByteBuffer in) throws Exception {
        value = Marshaller.readUuid(in);
    }

    /**
     * Factory to create {@link UuidMessageID} instances.
     */
    public static class Generator
        implements Message.IDGenerator
    {
        public Message.ID generate() {
            return new UuidMessageID();
        }
    }
}
