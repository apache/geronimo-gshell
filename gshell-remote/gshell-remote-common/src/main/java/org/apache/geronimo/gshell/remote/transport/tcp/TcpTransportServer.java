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

import org.apache.geronimo.gshell.remote.transport.TransportServer;
import org.apache.geronimo.gshell.remote.message.MessageVisitor;
import org.apache.mina.transport.socket.nio.SocketAcceptor;

/**
 * Provides TCP server-side support.
 *
 * @version $Rev$ $Date$
 */
public class TcpTransportServer
    extends TcpTransportSupport
    implements TransportServer
{
    protected final URI location;

    protected final InetSocketAddress address;

    protected SocketAcceptor acceptor;

    protected boolean bound;

    public TcpTransportServer(final URI location) throws Exception {
        assert location != null;

        this.location = location;
        this.address = new InetSocketAddress(InetAddress.getByName(location.getHost()), location.getPort());
    }

    protected synchronized void init() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        acceptor = new SocketAcceptor(Runtime.getRuntime().availableProcessors(), executor);
        acceptor.setLocalAddress(address);

        //
        // HACK: Need to manually wire in the visitor impl for now... :-(
        //

        setMessageVisitor((MessageVisitor) getContainer().lookup(MessageVisitor.class, "server"));

        configure(acceptor);

        //
        // TODO: Setup the authentication validation handler filter
        //
        
        // DefaultIoFilterChainBuilder filterChain = service.getFilterChain();
        // filterChain.addLast("auth", new AuthenticationFilter());
    }

    public synchronized void bind() throws Exception {
        if (bound) {
            throw new IllegalStateException("Already bound");
        }

        init();

        acceptor.bind();

        bound = true;

        log.info("Listening on: {}", address);
    }

    public synchronized void close() {
        acceptor.unbind();

        log.info("Closed");
    }

    public URI getLocation() {
        return location;
    }
}