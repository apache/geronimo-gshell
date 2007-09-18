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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.geronimo.gshell.remote.filter.LoggingFilter;
import org.apache.geronimo.gshell.remote.message.MessageCodecFactory;
import org.apache.geronimo.gshell.remote.transport.TransportServer;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides TCP server-side support.
 *
 * @version $Rev$ $Date$
 */
public class TcpTransportServer
    implements TransportServer
{
    protected Logger log = LoggerFactory.getLogger(getClass());

    protected TcpServerMessageVisitor messageVisitor;
    
    protected TcpProtocolHandler protocolHandler;

    protected URI location;

    protected InetSocketAddress address;

    protected SocketAcceptor acceptor;

    protected boolean bound;

    public TcpTransportServer(final URI location) throws Exception {
        assert location != null;

        this.location = location;
        this.address = new InetSocketAddress(InetAddress.getByName(location.getHost()), location.getPort());
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
        
        acceptor = new SocketAcceptor(Runtime.getRuntime().availableProcessors(), executor);
        acceptor.setLocalAddress(address);
        acceptor.setHandler(protocolHandler);

        DefaultIoFilterChainBuilder filterChain = acceptor.getFilterChain();

        filterChain.addLast("logger", new LoggingFilter());

        filterChain.addLast("protocol", new ProtocolCodecFilter(new MessageCodecFactory()));

        // filterChain.addLast("auth", new AuthenticationFilter());
    }

    //
    // NOTE: Setters exposed to support Plexus autowire()
    //

    public void setMessageVisitor(final TcpServerMessageVisitor messageVisitor) {
        log.debug("Using message visitor: {}", messageVisitor);

        this.messageVisitor = messageVisitor;
    }

    public void setProtocolHandler(final TcpProtocolHandler protocolHandler) {
        log.debug("Using protocol handler: {}", protocolHandler);

        this.protocolHandler = protocolHandler;
    }

    public void bind() throws Exception {
        if (bound) {
            throw new IllegalStateException("Already bound");
        }

        init();

        acceptor.bind();

        bound = true;

        log.info("Listening on: {}", address);
    }

    public URI getLocation() {
        return location;
    }

    //
    // TransportServer
    //

    public void close() {
        acceptor.unbind();
    }
}