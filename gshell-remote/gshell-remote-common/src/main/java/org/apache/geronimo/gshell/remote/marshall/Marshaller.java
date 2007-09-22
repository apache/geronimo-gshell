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

package org.apache.geronimo.gshell.remote.marshall;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.UUID;

import org.apache.mina.common.ByteBuffer;

/**
 * Provides support for marshalling and unmarshalling objects.
 *
 * @version $Rev$ $Date$
 */
public class Marshaller
{
    //
    // TODO: Should we maybe look at using XStream to handle cases when things don't marshall normally?
    //
    
    private final ByteBuffer buff;

    public Marshaller(final ByteBuffer buff) {
        assert buff != null;

        this.buff = buff;
    }

    public static MarshalAware unmarshal(final ByteBuffer in) throws Exception {
        assert in != null;

        //
        // HACK: This is broke... so just use the default for now...
        //

        /*
        Class type = (Class) readObject(in);

        MarshalAware obj = (MarshalAware) type.newInstance();

        obj.readExternal(in);
        
        return obj;
        */

        return (MarshalAware) readObject(in);
    }

    public MarshalAware unmarshal() throws Exception {
        return unmarshal(buff);
    }

    public static void marshal(final ByteBuffer out, final MarshalAware obj) throws Exception {
        assert out != null;

        //
        // HACK: This is broke... so just use the default for now...
        //

        /*
        writeObject(out, obj.getClass());

        obj.writeExternal(out);
        */

        writeObject(out, obj);
    }

    public void marshal(final MarshalAware obj) throws Exception {
        marshal(buff, obj);
    }

    //
    // Boolean Serialization
    //

    private static final byte TRUE = 1;

    private static final byte FALSE = 0;

    public static boolean readBoolean(final ByteBuffer in) {
        assert in != null;

        byte b = in.get();

        if (b == TRUE) {
            return true;
        }
        else if (b == FALSE) {
            return false;
        }
        else {
            throw new Error();
        }
    }

    public boolean getBoolean() {
        return readBoolean(buff);
    }

    public static void writeBoolean(final ByteBuffer out, final boolean bool) {
        assert out != null;

        if (bool) {
            out.put(TRUE);
        }
        else {
            out.put(FALSE);
        }
    }

    public void put(final boolean bool) {
        writeBoolean(buff, bool);
    }

    //
    // Byte[] Serialization
    //

    public static byte[] readBytes(final ByteBuffer in) {
        assert in != null;

        boolean isNull = readBoolean(in);

        if (isNull) {
            return null;
        }

        int len = in.getInt();

        byte[] bytes = new byte[len];

        in.get(bytes);

        return bytes;
    }

    public byte[] getBytes() {
        return readBytes(buff);
    }

    public static void writeBytes(final ByteBuffer out, final byte[] bytes) {
        assert out != null;

        if (bytes == null) {
            writeBoolean(out, true);
        }
        else {
            writeBoolean(out, false);

            out.putInt(bytes.length);

            out.put(bytes);
        }
    }

    public void put(final byte[] bytes) {
        writeBytes(buff, bytes);
    }

    //
    // ByteBuffer Serialization
    //

    public static ByteBuffer readBuffer(final ByteBuffer in) {
        assert in != null;

        byte[] bytes = readBytes(in);

        if (bytes == null) {
            return null;
        }

        return ByteBuffer.wrap(bytes);
    }

    public ByteBuffer getBuffer() {
        return readBuffer(buff);
    }

    public static void writeBuffer(final ByteBuffer out, final ByteBuffer buffer) {
        assert out != null;

        if (buffer == null) {
            writeBytes(out, null);
        }
        else {
            writeBoolean(out, false);

            out.putInt(buffer.remaining());

            out.put(buffer);
        }
    }

    public void put(final ByteBuffer b) {
        writeBuffer(buff, b);
    }

    //
    // Object Serialization
    //

    public static Object readObject(final ByteBuffer in) throws IOException, ClassNotFoundException {
        assert in != null;

        byte[] bytes = readBytes(in);

        if (bytes == null) {
            return null;
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);

        return ois.readObject();
    }

    public Object getObject() throws IOException, ClassNotFoundException {
        return readObject(buff);
    }

    public static void writeObject(final ByteBuffer out, final Object obj) throws IOException {
        assert out != null;

        byte[] bytes = null;

        if (obj != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            oos.writeObject(obj);
            oos.flush();

            bytes = baos.toByteArray();
        }

        writeBytes(out, bytes);
    }

    public void put(final Object obj) throws IOException {
        writeObject(buff, obj);
    }

    //
    // String Serialization
    //

    private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");

    public static String readString(final ByteBuffer in) throws CharacterCodingException {
        assert in != null;

        int len = in.getInt();

        if (len == -1) {
            return null;
        }

        return in.getString(len, UTF_8_CHARSET.newDecoder());
    }

    public String getString() throws CharacterCodingException {
        return readString(buff);
    }

    public static void writeString(final ByteBuffer out, final String str) throws CharacterCodingException {
        assert out != null;

        if (str == null) {
            out.putInt(-1);
        }
        else {
            int len = str.length();
            out.putInt(len);

            out.putString(str, len, UTF_8_CHARSET.newEncoder());
        }
    }

    public void put(final String str) throws CharacterCodingException {
        writeString(buff, str);
    }

    //
    // UUID Serialization
    //

    public static UUID readUuid(final ByteBuffer in) {
        assert in != null;

        boolean isNull = readBoolean(in);

        if (isNull) {
            return null;
        }

        long msb = in.getLong();

        long lsb = in.getLong();

        return new UUID(msb, lsb);
    }

    public UUID getUuid() {
        return readUuid(buff);
    }

    public static void writeUuid(final ByteBuffer out, final UUID uuid) {
        assert out != null;

        if (uuid == null) {
            writeBoolean(out, true);
        }
        else {
            writeBoolean(out, false);

            out.putLong(uuid.getMostSignificantBits());

            out.putLong(uuid.getLeastSignificantBits());
        }
    }

    public void put(final UUID uuid) {
        writeUuid(buff, uuid);
    }

    //
    // Enum Serialization (adapted from Mina 2.x)
    //

    public static ByteBuffer writeEnum(final ByteBuffer out, Enum<?> e) {
        if (e.ordinal() > Byte.MAX_VALUE) {
            throw new IllegalArgumentException(enumConversionErrorMessage(e, "byte"));
        }

        return out.put((byte) e.ordinal());
    }

    public void put(Enum<?> e) {
        writeEnum(buff, e);
    }

    public static <E extends Enum<E>> E readEnum(final ByteBuffer in, final Class<E> enumClass) {
        return toEnum(enumClass, in.get());
    }

    public <E extends Enum<E>> E getEnum(final Class<E> enumClass) {
        return readEnum(buff, enumClass);
    }

    private static <E> E toEnum(Class<E> enumClass, int i) {
        E[] enumConstants = enumClass.getEnumConstants();
        if (i > enumConstants.length) {
            throw new IndexOutOfBoundsException(String.format("%d is too large of an ordinal to convert to the enum %s", i, enumClass.getName()));
        }
        return enumConstants[i];
    }

    private static String enumConversionErrorMessage(Enum<?> e, String type) {
        return String.format("%s.%s has an ordinal value too large for a %s", e.getClass().getName(), e.name(), type);
    }
}