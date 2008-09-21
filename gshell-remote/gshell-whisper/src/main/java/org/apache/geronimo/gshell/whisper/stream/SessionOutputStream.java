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

import org.apache.geronimo.gshell.whisper.util.SessionAttributeBinder;
import org.apache.geronimo.gshell.yarn.Yarn;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An {@link OutputStream} that forwards all write operations as {@link StreamMessage} messages.
 *
 * @version $Rev$ $Date$
 */
public class SessionOutputStream
    extends OutputStream
{
    public static final SessionAttributeBinder<SessionOutputStream> BINDER = new SessionAttributeBinder<SessionOutputStream>(SessionOutputStream.class);

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final IoSession session;

    private WriteFuture lastWriteFuture;

    private volatile boolean opened;

    public SessionOutputStream(final IoSession session) {
        assert session != null;
        
        this.session = session;

        this.opened = true;
    }

    public String toString() {
        return Yarn.render(this);
    }

    public void close() throws IOException {
        if (!opened) {
            return;
        }
        
        try {
            flush();

            opened = false;
        }
        finally {
            super.close();
        }
    }

    public void write(final byte[] b, final int off, final int len) throws IOException {
        write(ByteBuffer.wrap(b.clone(), off, len));
    }

    public void write(final int b) throws IOException {
        ByteBuffer buff = ByteBuffer.allocate(1);
        buff.put((byte) b);
        buff.flip();

        write(buff);
    }

    private void ensureOpened() throws IOException {
        if (!opened || !session.isConnected()) {
            throw new IOException("The session has been closed.");
        }
    }
    
    private synchronized void write(final ByteBuffer buff) throws IOException {
        ensureOpened();

        log.debug("Writing {} bytes", buff.remaining());

        WriteFuture wf = session.write(new StreamMessage(buff));

        lastWriteFuture = wf;

        wf.join();
    }

    public synchronized void flush() throws IOException {
        ensureOpened();

        if (lastWriteFuture == null) {
            return;
        }
        
        // Process the last write future and clear it
        lastWriteFuture.join();

        if (!lastWriteFuture.isWritten()) {
            throw new IOException("Failed to fully write bytes to the session");
        }
    }
}
