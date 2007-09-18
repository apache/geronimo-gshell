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
import java.io.OutputStream;

import org.apache.geronimo.gshell.remote.message.WriteStreamMessage;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;
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
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final IoSession session;

    private WriteFuture lastWriteFuture;

    public SessionOutputStream(final IoSession session) {
        assert session != null;
        
        this.session = session;
    }

    @Override
    public void close() throws IOException {
        try {
            flush();
        }
        finally {
            super.close();
        }
    }

    private void ensureOpened() throws IOException {
        if (!session.isConnected()) {
            throw new IOException("The session has been closed.");
        }
    }

    private synchronized void write(final ByteBuffer buff) throws IOException {
        ensureOpened();

        log.debug("Writing stream from: {}", buff);

        WriteStreamMessage msg = new WriteStreamMessage(buff);

        lastWriteFuture = session.write(msg);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        write(ByteBuffer.wrap(b.clone(), off, len));
    }

    @Override
    public void write(final int b) throws IOException {
        ByteBuffer buff = ByteBuffer.allocate(1);

        buff.put((byte) b);

        buff.flip();

        write(buff);
    }

    @Override
    public synchronized void flush() throws IOException {
        if (lastWriteFuture == null) {
            return;
        }

        log.debug("Flushing stream...");
        
        lastWriteFuture.awaitUninterruptibly();
        
        if (!lastWriteFuture.isWritten()) {
            throw new IOException("The bytes could not be written to the session");
        }

        log.debug("Flushed");
    }
}
