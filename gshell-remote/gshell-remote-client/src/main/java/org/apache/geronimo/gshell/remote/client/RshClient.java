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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.geronimo.gshell.remote.message.EchoMessage;
import org.apache.geronimo.gshell.remote.message.HandShakeMessage;
import org.apache.geronimo.gshell.remote.message.Message;
import org.apache.geronimo.gshell.remote.message.MessageResponseInspector;
import org.apache.geronimo.gshell.remote.message.codec.MessageCodecFactory;
import org.apache.geronimo.gshell.remote.ssl.BogusSSLContextFactory;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.reqres.Request;
import org.apache.mina.filter.reqres.RequestResponseFilter;
import org.apache.mina.filter.reqres.Response;
import org.apache.mina.filter.ssl.SSLFilter;
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
    public static final int CONNECT_TIMEOUT = 3000;
    
    private Logger log = LoggerFactory.getLogger(getClass());

    @Requirement(role=IoHandler.class, hint="rsh-client")
    private RshClientProtocolHandler handler;

    private boolean ssl = false;
    
    private InetSocketAddress address;

    private SocketConnector connector;

    private IoSession session;

    private boolean connected = false;

    public void connect(final String hostname, final int port) throws Exception {
        if (connected) {
            throw new IllegalStateException("Already connected");
        }

        address = new InetSocketAddress(hostname, port);

        handler.setVisitor(new RshClientMessageVisitor());

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        connector = new SocketConnector(Runtime.getRuntime().availableProcessors(), executor);
        
        connector.setConnectTimeout(30);
        connector.setHandler(handler);

        DefaultIoFilterChainBuilder filterChain = connector.getFilterChain();
        filterChain.addLast("logger", new LoggingFilter());
        filterChain.addLast("protocol", new ProtocolCodecFilter(new MessageCodecFactory()));

        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
        filterChain.addLast("reqres", new RequestResponseFilter(new MessageResponseInspector(), scheduler));

        if (ssl) {
            SSLFilter sslFilter = new SSLFilter(BogusSSLContextFactory.getInstance(true));
            sslFilter.setUseClientMode(true);
            
            filterChain.addFirst("sslFilter", sslFilter);
        }

        log.info("Connecting to: {}", address);

        ConnectFuture cf = connector.connect(address);
        if (cf.awaitUninterruptibly(CONNECT_TIMEOUT)) {
             session = cf.getSession();
        }
        else {
            throw new RuntimeException("Failed to connect");
        }

        log.info("Connected");
        
        connected = true;
    }

    public boolean isConnected() {
        return connected;
    }
    
    private void ensureConnected() {
        if (!connected) {
            throw new IllegalStateException("Not connected");
        }

        if (!session.isConnected()) {
            throw new IllegalStateException("Session not connected");
        }

        if (session.isClosing()) {
            throw new IllegalStateException("Session is closing");
        }
    }

    public void echo(final String text) {
        ensureConnected();

        log.debug("Echoing: {}", text);

        WriteFuture wf = session.write(new EchoMessage(text));

        wf.awaitUninterruptibly();

        if (!wf.isWritten()) {
            log.error("Failed to send request; session did not fully write the message");
        }
    }

    protected Response request(final Message msg, final long timeout, final TimeUnit unit) throws InterruptedException {
        Request req = new Request(msg.getId(), msg, timeout, unit);

        WriteFuture wf = session.write(req);
        wf.awaitUninterruptibly();

        if (!wf.isWritten()) {
            log.error("Failed to send request; session did not fully write the message");
        }

        return req.awaitResponse();
    }

    public void handshake() throws Exception {
        ensureConnected();

        log.info("Starting handshake");
        
        HandShakeMessage msg = new HandShakeMessage();

        Response resp = request(msg, 5, TimeUnit.SECONDS);

        log.info("Response: {}", resp.getMessage());
    }
}