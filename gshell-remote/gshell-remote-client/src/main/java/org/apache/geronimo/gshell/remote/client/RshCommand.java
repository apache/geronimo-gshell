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

package org.apache.geronimo.gshell.remote.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Date;

import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
@CommandComponent(id="rsh")
public class RshCommand
    extends CommandSupport
{
    @Requirement
    private RshClientFactory factory;

    private RshClient client;

    protected Object doExecute() throws Exception {
        URI location = new URI("ssl://localhost:9999");

        io.out.println("Connecting to: " + location);

        client = factory.connect(location);

        io.out.println("Connected");
        
        client.echo("TESTING");

        client.handshake();

        client.echo("READ_STREAMS");

        OutputStream out = client.getOutputStream();
        final PrintWriter writer = new PrintWriter(out);

        InputStream in = client.getInputStream();
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
                        writer.println("FROM CLIENT: " + new Date());
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

        t.join();
        t2.join();
        
        client.close();
        
        return SUCCESS;
    }
}