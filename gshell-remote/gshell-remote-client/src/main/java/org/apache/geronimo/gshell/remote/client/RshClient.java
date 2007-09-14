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

import java.net.InetSocketAddress;

import org.apache.geronimo.gshell.remote.message.EchoMessage;
import org.apache.geronimo.gshell.remote.message.codec.MessageCodecFactory;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
@Component(role=RshClient.class)
public class RshClient
{
    private Logger log = LoggerFactory.getLogger(getClass());

    @Requirement(role=IoHandler.class, hint="rsh-client")
    private RshClientProtocolHandlerSupport handler;

    private InetSocketAddress address;

    private SocketConnector connector;

    private IoSession session;

    private boolean connected = false;

    public void connect(final String hostname, final int port) throws Exception {
        if (connected) {
            throw new IllegalStateException("Already connected");
        }

        address = new InetSocketAddress(hostname, port);

        connector = new SocketConnector();
        connector.setConnectTimeout(30);
        connector.setHandler(handler);

        DefaultIoFilterChainBuilder filterChain = connector.getFilterChain();
        filterChain.addLast("logger", new LoggingFilter());
        filterChain.addLast("protocol", new ProtocolCodecFilter(new MessageCodecFactory()));

        log.info("Connecting to: {}", address);

        ConnectFuture cf = connector.connect(address);
        cf.await();

        session = cf.getSession();

        log.info("Connected");
        
        connected = true;
    }

    private void ensureConnected() {
        if (!connected) {
            throw new IllegalStateException("Not connected");
        }
    }

    public void echo(final String text) {
        ensureConnected();

        session.write(new EchoMessage(text));
    }
}