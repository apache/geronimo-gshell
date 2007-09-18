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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.geronimo.gshell.remote.message.EchoMessage;
import org.apache.geronimo.gshell.remote.message.ExecuteMessage;
import org.apache.geronimo.gshell.remote.message.HandShakeMessage;
import org.apache.geronimo.gshell.remote.message.Message;
import org.apache.geronimo.gshell.remote.transport.Transport;
import org.apache.geronimo.gshell.remote.transport.TransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides support for the client-side of the remote shell protocol.
 *
 * @version $Rev$ $Date$
 */
public class RshClient
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Transport transport;

    public RshClient(final URI location, final TransportFactory factory) throws Exception {
        assert location != null;
        assert factory != null;

        transport = factory.connect(location);

        log.debug("Connected to: {}", location);
    }

    public void echo(final String text) throws Exception {
        log.debug("Echoing: {}", text);

        transport.send(new EchoMessage(text));
    }

    public void handshake() throws Exception {
        log.info("Starting handshake");

        Message resp = transport.request(new HandShakeMessage());

        log.info("Response: {}", resp);
    }

    public void execute(final String line) throws Exception {
        log.info("Executing: {}", line);

        transport.send(new ExecuteMessage(line));

        //
        // TODO: Need to handle the command result
        //
    }

    public InputStream getInputStream() {
        return transport.getInputStream();
    }

    public OutputStream getOutputStream() {
        return transport.getOutputStream();
    }

    public void close() {
        transport.close();

        log.debug("Closed");
    }
}