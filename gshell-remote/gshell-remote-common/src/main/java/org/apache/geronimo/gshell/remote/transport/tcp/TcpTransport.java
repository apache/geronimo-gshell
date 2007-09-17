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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.geronimo.gshell.remote.filter.LoggingFilter;
import org.apache.geronimo.gshell.remote.message.Message;
import org.apache.geronimo.gshell.remote.message.MessageCodecFactory;
import org.apache.geronimo.gshell.remote.transport.Transport;
import org.apache.mina.common.CloseFuture;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.reqres.Request;
import org.apache.mina.filter.reqres.RequestResponseFilter;
import org.apache.mina.filter.reqres.Response;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class TcpTransport
    implements Transport
{
    private static final int CONNECT_TIMEOUT = 3000;

    protected Logger log = LoggerFactory.getLogger(getClass());

    protected TcpClientMessageVisitor messageVisitor;

    protected TcpProtocolHandler protocolHandler;

    protected URI remoteLocation;

    protected InetSocketAddress remoteAddress;

    protected URI localLocation;

    protected InetSocketAddress localAddress;

    protected SocketConnector connector;

    protected IoSession session;

    protected boolean connected;

    public TcpTransport(final URI remote, final URI local) throws Exception {
        assert remote != null;
        // local may be null

        this.remoteLocation = remote;
        this.remoteAddress = new InetSocketAddress(InetAddress.getByName(remote.getHost()), remote.getPort());

        if (local != null) {
            this.localLocation = local;
            this.localAddress = new InetSocketAddress(InetAddress.getByName(local.getHost()), local.getPort());
        }
    }

    protected void init() throws Exception {
        if (protocolHandler == null) {
            throw new IllegalStateException("Protocol handler not injected");
        }
        if (messageVisitor == null) {
            throw new IllegalStateException("Message visitor not injected");
        }
        
        protocolHandler.setVisitor(messageVisitor);

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);

        connector = new SocketConnector(Runtime.getRuntime().availableProcessors(), executor);
        connector.setConnectTimeout(30);
        connector.setHandler(protocolHandler);

        DefaultIoFilterChainBuilder filterChain = connector.getFilterChain();

        filterChain.addLast("logger", new LoggingFilter());

        filterChain.addLast("protocol", new ProtocolCodecFilter(new MessageCodecFactory()));

        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());

        filterChain.addLast("reqres", new RequestResponseFilter(protocolHandler.getResponseInspector(), scheduler));
    }

    //
    // NOTE: Setters exposed to support Plexus autowire()
    //

    public void setMessageVisitor(final TcpClientMessageVisitor messageVisitor) {
        log.debug("Using message visitor: {}", messageVisitor);

        this.messageVisitor = messageVisitor;
    }

    public void setProtocolHandler(final TcpProtocolHandler protocolHandler) {
        log.debug("Using protocol handler: {}", protocolHandler);

        this.protocolHandler = protocolHandler;
    }

    public void connect() throws Exception {
        if (connected) {
            throw new IllegalStateException("Already connected");
        }
        
        init();

        log.info("Connecting to: {}", remoteAddress);

        ConnectFuture cf = connector.connect(remoteAddress, localAddress);

        if (cf.awaitUninterruptibly(CONNECT_TIMEOUT)) {
             session = cf.getSession();
        }
        else {
            throw new Exception("Failed to connect");
        }

        connected = true;
        
        log.info("Connected");
    }

    public URI getRemoteLocation() {
        return remoteLocation;
    }

    public URI getLocalLocation() {
        return localLocation;
    }

    //
    // Transport
    //

    private void doSend(final Object msg) throws Exception {
        assert msg != null;

        WriteFuture wf = session.write(msg);

        wf.awaitUninterruptibly();

        if (!wf.isWritten()) {
            throw new IOException("Session did not fully write the message");
        }
    }

    public void send(final Message msg) throws Exception {
        assert msg != null;

        doSend(msg);
    }

    public Message request(final Message msg) throws Exception {
        assert msg != null;

        Request req = new Request(msg.getId(), msg, 1, TimeUnit.SECONDS);

        doSend(req);

        Response resp = req.awaitResponse();

        return (Message) resp.getMessage();
    }

    public InputStream getInputStream() {
        return (InputStream) session.getAttribute(Transport.INPUT_STREAM);
    }

    public OutputStream getOutputStream() {
        return (OutputStream) session.getAttribute(Transport.OUTPUT_STREAM);
    }

    public void close() {
        CloseFuture cf = session.close();

        cf.awaitUninterruptibly();
    }
}