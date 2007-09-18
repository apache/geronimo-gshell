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

package org.apache.geronimo.gshell.remote.transport.tcp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;

import org.apache.geronimo.gshell.remote.message.EchoMessage;
import org.apache.geronimo.gshell.remote.message.HandShakeMessage;
import org.apache.geronimo.gshell.remote.transport.Transport;
import org.apache.mina.common.IoSession;
import org.codehaus.plexus.component.annotations.Component;

/**
 * Defines the logic for server-side message processing.
 *
 * @version $Rev$ $Date$
 */
@Component(role=TcpServerMessageVisitor.class)
public class TcpServerMessageVisitor
    extends TcpMessageVisitorSupport
{
    public void visitEcho(final EchoMessage msg) throws Exception {
        assert msg != null;

        String text = msg.getText();

        //
        // HACK: This is just here to test out the stream io muck
        //

        if ("READ_STREAMS".equals(text)) {
            final IoSession session = msg.getSession();

            OutputStream out = (OutputStream) session.getAttribute(Transport.OUTPUT_STREAM);
            final PrintWriter writer = new PrintWriter(out);

            InputStream in = (InputStream) session.getAttribute(Transport.INPUT_STREAM);
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
            log.info("ECHOING: {}", text);

            msg.reply(new EchoMessage(text));
        }
    }

    public void visitHandShake(final HandShakeMessage msg) throws Exception {
        assert msg != null;

        log.info("HANDSHAKE");

        msg.reply(new EchoMessage("SUCCESS"));
    }
}