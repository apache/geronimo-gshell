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

import java.util.Arrays;

import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.filter.codec.ProtocolCodecException;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class DefaultMessageHeader
    implements MessageHeader
{
    private final byte[] magic;

    private final byte version;

    /*
    private MessageType type;

    private int length;
    */

    public DefaultMessageHeader(final byte[] magic, final byte version) {
        this.magic = magic;
        this.version = version;
    }

    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }
    
    public byte[] magic() {
        return magic;
    }

    public byte version() {
        return version;
    }

    /*
    public MessageType type() {
        return type;
    }

    public int length() {
        return length;
    }
    */

    public int size() {
        return magic().length + /* version */ 1; //  + /* message type */ type.size() + /* length */ 4;
    }

    protected void writeMagic(final ByteBuffer out) throws Exception {
        assert out != null;

        out.put(magic());
    }

    protected byte[] readMagic(final ByteBuffer in) throws Exception {
        assert in != null;

        byte[] bytes = new byte[magic().length];

        in.get(bytes);

        if (!Arrays.equals(magic(), bytes)) {
            throw new InvalidMagicException(bytes);
        }

        return bytes;
    }

    protected void writeVersion(final ByteBuffer out) throws Exception {
        assert out != null;

        out.put(version());
    }

    protected byte readVersion(final ByteBuffer in) throws Exception {
        assert in != null;

        byte version = in.get();

        if (version() != version) {
            throw new InvalidVersionException(version);
        }

        return version;
    }

    public void writeExternal(final ByteBuffer out) throws Exception {
        writeMagic(out);
        writeVersion(out);
    }

    public void readExternal(final ByteBuffer in) throws Exception {
        readMagic(in);
        readVersion(in);
    }

    public class InvalidMagicException
        extends ProtocolCodecException
    {
        public InvalidMagicException(final byte[] bytes) {
            //
            // TODO:
            //
        }
    }

    public class InvalidVersionException
        extends ProtocolCodecException
    {
        public InvalidVersionException(final byte version) {
            //
            // TODO:
            //
        }
    }
}