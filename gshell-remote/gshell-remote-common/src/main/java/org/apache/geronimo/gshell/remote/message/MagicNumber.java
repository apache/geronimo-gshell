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

import java.util.Arrays;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.filter.codec.ProtocolCodecException;

/**
 * Helper to deal with the protocol magic number.
 *
 * @version $Rev$ $Date$
 */
public class MagicNumber
{
    public final static byte BYTES[] = { 'g', 's', 'h', 0 };

    public static byte[] read(final ByteBuffer in) throws InvalidMagicNumberException {
        assert in != null;

        byte[] bytes = new byte[BYTES.length];
        in.get(bytes);

        validate(bytes);

        return bytes;
    }

    public static void validate(final byte[] bytes) throws InvalidMagicNumberException {
        if (!Arrays.equals(bytes, BYTES)) {
            throw new InvalidMagicNumberException();
        }
    }

    public static void write(final ByteBuffer out) throws InvalidMagicNumberException {
        assert out != null;
        
        out.put(BYTES);
    }

    //
    // InvalidMagicNumberException
    //

    public static class InvalidMagicNumberException
        extends ProtocolCodecException
    {
        public InvalidMagicNumberException() {
            super();
        }
    }
}