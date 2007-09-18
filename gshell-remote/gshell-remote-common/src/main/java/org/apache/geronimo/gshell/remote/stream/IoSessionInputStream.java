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

//
// NOTE: Snatched from Apache Mina
//

package org.apache.geronimo.gshell.remote.stream;

import java.io.IOException;
import java.io.InputStream;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;

/**
 * An {@link InputStream} that buffers data read from
 * {@link IoHandler#messageReceived(IoSession,Object)} events.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev$, $Date$
 */
public class IoSessionInputStream
    extends InputStream
{
    private final Object mutex = new Object();

    private final ByteBuffer buff;

    private volatile boolean closed;

    private volatile boolean released;

    private IOException exception;

    public IoSessionInputStream() {
        buff = ByteBuffer.allocate(16);
        buff.setAutoExpand(true);
        buff.limit(0);
    }

    @Override
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

    @Override
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

    @Override
    public int read() throws IOException {
        synchronized (mutex) {
            if (!waitForData()) {
                return -1;
            }

            return buff.get() & 0xff;
        }
    }

    @Override
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
                    IOException n = new IOException("Interrupted while waiting for more data");
                    n.initCause(e);
                    throw n;
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

    public void write(final ByteBuffer src) {
        synchronized (mutex) {
            if (closed) {
                return;
            }

            if (buff.hasRemaining()) {
                this.buff.compact();
                this.buff.put(src);
                this.buff.flip();
            }
            else {
                this.buff.clear();
                this.buff.put(src);
                this.buff.flip();
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