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

package org.apache.geronimo.gshell.remote.stream;

//
// NOTE: Snatched and massaged from Apache Mina
//

import java.io.IOException;
import java.io.OutputStream;

import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.remote.message.Message;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.util.NewThreadExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link OutputStream} that forwards all write operations as {@link WriteStreamMessage} messages.
 *
 * @version $Rev$ $Date$
 */
public class SessionOutputStream
    extends OutputStream
{
    private static final Logger log = LoggerFactory.getLogger(SessionOutputStream.class);

    private final Object mutex = new Object();

    private final IoSession session;

    private WriteFuture lastWriteFuture;

    private volatile boolean opened;

    public SessionOutputStream(final IoSession session) {
        assert session != null;
        
        this.session = session;

        this.opened = true;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
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

    private void write(final ByteBuffer buff) throws IOException {
        synchronized (mutex) {
            ensureOpened();

            log.debug("Writing: {}", buff);

            Message msg = new WriteStreamMessage(buff);

            //
            // TODO: See if we should hold on to each of the write futures what we go through before flushing?
            //
            
            final WriteFuture wf = session.write(msg);

            lastWriteFuture = wf;

            log.debug("Done");
            
            new NewThreadExecutor().execute(new Runnable() {
                public void run() {
                    log.debug("Waiting for full write");

                    wf.join();

                    log.debug("Completed; written: {}", wf.isWritten());
                }
            });
        }
    }

    public void flush() throws IOException {
        synchronized (mutex) {
            ensureOpened();

            if (lastWriteFuture == null) {
                return;
            }

            log.debug("Flushing");

            // Process the last write future and clear it
            WriteFuture wf = lastWriteFuture;
            lastWriteFuture = null;

            wf.join();

            if (!wf.isWritten()) {
                throw new IOException("Failed to fully write bytes to the session");
            }

            log.debug("Flushed");
        }
    }

    //
    // Session Access
    //

    public static SessionOutputStream lookup(final IoSession session) {
        assert session != null;

        SessionOutputStream out = (SessionOutputStream) session.getAttribute(SessionOutputStream.class.getName());

        if (out == null) {
            throw new IllegalStateException("Output stream not bound");
        }

        return out;
    }

    public static void bind(final IoSession session, final SessionOutputStream out) {
        assert session != null;
        assert out != null;

        Object obj = session.getAttribute(SessionOutputStream.class.getName());

        if (obj != null) {
            throw new IllegalStateException("Output stream already bound");
        }

        session.setAttribute(SessionOutputStream.class.getName(), out);

        log.trace("Bound output stream: {}", out);
    }

    public static SessionOutputStream unbind(final IoSession session) {
        assert session != null;

        return (SessionOutputStream) session.removeAttribute(SessionOutputStream.class.getName());
    }
}
