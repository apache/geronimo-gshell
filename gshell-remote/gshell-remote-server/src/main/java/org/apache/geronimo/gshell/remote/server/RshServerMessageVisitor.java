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

package org.apache.geronimo.gshell.remote.server;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;

import org.apache.geronimo.gshell.remote.RshProtocolHandlerSupport;
import org.apache.geronimo.gshell.remote.message.EchoMessage;
import org.apache.geronimo.gshell.remote.message.HandShakeMessage;
import org.apache.geronimo.gshell.remote.message.MessageVisitorAdapter;
import org.apache.geronimo.gshell.remote.message.WriteStreamMessage;
import org.apache.geronimo.gshell.remote.stream.IoSessionInputStream;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class RshServerMessageVisitor
    extends MessageVisitorAdapter
{
    private Logger log = LoggerFactory.getLogger(getClass());

    public void visitEcho(final EchoMessage msg) {
        assert msg != null;

        final IoSession session = (IoSession) msg.getAttachment();
        assert session != null;
        
        String text = msg.getText();

        //
        // HACK: This is just here to test out the stream io muck
        //

        if ("READ_STREAMS".equals(text)) {
            OutputStream out = (OutputStream) session.getAttribute(RshProtocolHandlerSupport.OUTPUT_STREAM);
            final PrintWriter writer = new PrintWriter(out);

            InputStream in = (InputStream) session.getAttribute(RshProtocolHandlerSupport.INPUT_STREAM);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            Thread t = new Thread("Stream Consumer") {
                public void run() {
                    try {
                        log.debug("Consumer running...");

                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.err.println(line);
                        }
                        
                        log.debug("Consumer stopped");
                    }
                    catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            };

            t.start();

            Thread t2 = new Thread("Noise Maker") {
                public void run() {
                    try {
                        log.debug("Noise Maker...");

                        while (true) {
                            writer.println("FROM SERVER: " + new Date());
                            writer.flush();

                            Thread.sleep(1000 * 5);
                        }

                        // log.debug("Noise Maker stopped");
                    }
                    catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            };

            t2.start();
        }
        else {
            log.info("ECHO: {}", text);
        }
    }

    public void visitHandShake(final HandShakeMessage msg) {
        assert msg != null;

        log.info("HANDSHAKE");
        
        IoSession session = (IoSession) msg.getAttachment();
        assert session != null;

        // For now just echo something back, with the same ID
        EchoMessage resp = new EchoMessage("SUCCESS");
        resp.setId(msg.getId());

        session.write(resp);
    }

    public void visitWriteStream(final WriteStreamMessage msg) {
        assert msg != null;

        IoSession session = (IoSession) msg.getAttachment();
        assert session != null;

        // Look up the bound stream in the session context
        String key = RshProtocolHandlerSupport.STREAM_BASENAME + msg.getName();
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
            ByteBuffer buff = msg.getBuffer();
            in.write(buff);
        }
    }
}