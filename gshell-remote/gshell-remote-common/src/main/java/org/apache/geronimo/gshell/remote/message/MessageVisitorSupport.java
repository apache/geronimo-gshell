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

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.geronimo.gshell.remote.stream.IoSessionInputStream;
import org.apache.geronimo.gshell.remote.transport.Transport;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides support for {@link MessageVisitor} implementations.
 *
 * @version $Rev$ $Date$
 */
public abstract class MessageVisitorSupport
    extends MessageVisitorAdapter
{
    protected Logger log = LoggerFactory.getLogger(getClass());

    //
    // Stream Access
    //

    protected InputStream getInputStream(final IoSession session) {
        assert session != null;

        InputStream in = (InputStream) session.getAttribute(Transport.INPUT_STREAM);

        if (in == null) {
            throw new IllegalStateException("Input stream not bound");
        }

        return in;
    }

    protected OutputStream getOutputStream(final IoSession session) {
        assert session != null;

        OutputStream out = (OutputStream) session.getAttribute(Transport.OUTPUT_STREAM);

        if (out == null) {
            throw new IllegalStateException("Output stream not bound");
        }


        return out;
    }

    //
    // MessageVisitor
    //
    
    public void visitWriteStream(final WriteStreamMessage msg) throws Exception {
        assert msg != null;

        IoSession session = msg.getSession();

        // Look up the bound stream in the session context
        String key = Transport.STREAM_BASENAME + msg.getName();
        Object stream = session.getAttribute(key);

        // For now lets not toss any exceptions or send back any fault messages
        if (stream == null) {
            log.error("Stream is not registered: {}", key);
        }
        else if (!(stream instanceof IoSessionInputStream)) {
            log.error("Stream is not for input: {}", key);
        }
        else {
            IoSessionInputStream in = (IoSessionInputStream)stream;
            in.write(msg.getBuffer());
        }
    }
}