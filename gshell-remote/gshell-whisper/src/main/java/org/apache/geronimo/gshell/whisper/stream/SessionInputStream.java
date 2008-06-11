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

package org.apache.geronimo.gshell.whisper.stream;

//
// NOTE: Snatched and massaged from Apache Mina
//

import java.io.IOException;
import java.io.InputStream;

import org.apache.geronimo.gshell.io.NestedIOException;
import org.apache.geronimo.gshell.yarn.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.yarn.ToStringStyle;
import org.apache.geronimo.gshell.whisper.util.SessionAttributeBinder;
import org.apache.mina.common.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link InputStream} that buffers data read from {@link StreamMessage} messages.
 *
 * @version $Rev$ $Date$
 */
public class SessionInputStream
    extends InputStream
{
    public static final SessionAttributeBinder<SessionInputStream> BINDER = new SessionAttributeBinder<SessionInputStream>(SessionInputStream.class);

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Object mutex = new Object();

    private final ByteBuffer buff;

    private volatile boolean closed;

    private volatile boolean released;

    private IOException exception;

    public SessionInputStream() {
        buff = ByteBuffer.allocate(16);
        buff.setAutoExpand(true);
        buff.limit(0);
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public int available() {
        if (released) {
            return 0;
        }
        else {
            synchronized (mutex) {
                return buff.remaining();
            }
        }
    }

    public void close() {
        if (closed) {
            return;
        }

        synchronized (mutex) {
            closed = true;

            releaseBuffer();

            mutex.notifyAll();
        }
    }

    public int read() throws IOException {
        synchronized (mutex) {
            if (!waitForData()) {
                return -1;
            }

            return buff.get() & 0xff;
        }
    }

    public int read(final byte[] b, final int off, final int len) throws IOException {
        synchronized (mutex) {
            if (!waitForData()) {
                return -1;
            }

            int readBytes;

            if (len > buff.remaining()) {
                readBytes = buff.remaining();
            }
            else {
                readBytes = len;
            }

            buff.get(b, off, readBytes);

            return readBytes;
        }
    }

    private boolean waitForData() throws IOException {
        if (released) {
            return false;
        }

        synchronized (mutex) {
            while (!released && buff.remaining() == 0 && exception == null) {
                try {
                    mutex.wait();
                }
                catch (InterruptedException e) {
                    throw new NestedIOException("Interrupted while waiting for more data");
                }
            }
        }

        if (exception != null) {
            releaseBuffer();

            throw exception;
        }

        if (closed && buff.remaining() == 0) {
            releaseBuffer();

            return false;
        }

        return true;
    }

    private void releaseBuffer() {
        if (released) {
            return;
        }

        released = true;
    }

    public void write(final StreamMessage msg) {
        synchronized (mutex) {
            if (closed) {
                return;
            }

            ByteBuffer src = msg.getBuffer();

            log.debug("Writing {} bytes", src.remaining());

            if (buff.hasRemaining()) {
                buff.compact();
                buff.put(src);
                buff.flip();
            }
            else {
                buff.clear();
                buff.put(src);
                buff.flip();

                mutex.notifyAll();
            }
        }
    }

    public void throwException(final IOException e) {
        synchronized (mutex) {
            if (exception == null) {
                exception = e;

                mutex.notifyAll();
            }
        }
    }
}