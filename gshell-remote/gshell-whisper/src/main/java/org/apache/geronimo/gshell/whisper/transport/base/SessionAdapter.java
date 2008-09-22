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

package org.apache.geronimo.gshell.whisper.transport.base;

import org.apache.geronimo.gshell.chronos.Duration;
import org.apache.geronimo.gshell.whisper.message.Message;
import org.apache.geronimo.gshell.whisper.request.Requestor;
import org.apache.geronimo.gshell.whisper.stream.SessionInputStream;
import org.apache.geronimo.gshell.whisper.stream.SessionOutputStream;
import org.apache.geronimo.gshell.whisper.transport.Session;
import org.apache.geronimo.gshell.yarn.Yarn;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Adapts a MINA IoSession to a Whipser transport Session.
 *
 * @version $Rev$ $Date$
 */
public class SessionAdapter
    implements Session
{
    private final IoSession session;

    private boolean closed;

    public SessionAdapter(final IoSession session) {
        assert session != null;

        this.session = session;
    }
    
    public String toString() {
        return Yarn.render(this);
    }

    public IoSession getSession() {
        ensureOpened();

        return session;
    }

    public synchronized boolean isClosed() {
        return closed;
    }

    protected void ensureOpened() {
        if (isClosed()) {
            throw new IllegalStateException("Closed");
        }
    }

    public synchronized void close() {
        if (isClosed()) {
            return;
        }

        session.close();

        closed = true;
    }

    public WriteFuture send(final Object msg) throws Exception {
        assert msg != null;

        ensureOpened();

        return session.write(msg);
    }

    public Message request(final Message msg) throws Exception {
        assert msg != null;

        ensureOpened();

        Requestor requestor = new Requestor(this);

        return requestor.request(msg);
    }

    public Message request(final Message msg, final Duration timeout) throws Exception {
        assert msg != null;
        assert timeout != null;

        ensureOpened();

        Requestor requestor = new Requestor(this);

        return requestor.request(msg, timeout);
    }

    public InputStream getInputStream() {
        ensureOpened();

        return SessionInputStream.BINDER.lookup(session);
    }

    public OutputStream getOutputStream() {
        ensureOpened();

        return SessionOutputStream.BINDER.lookup(session);
    }

    public OutputStream getErrorStream() {
        ensureOpened();

        throw new UnsupportedOperationException();
    }
}