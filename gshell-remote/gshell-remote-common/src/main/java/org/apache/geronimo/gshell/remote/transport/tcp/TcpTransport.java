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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.geronimo.gshell.remote.message.Message;
import org.apache.geronimo.gshell.remote.message.MessageVisitor;
import org.apache.geronimo.gshell.remote.request.Requestor;
import org.apache.geronimo.gshell.remote.stream.SessionInputStream;
import org.apache.geronimo.gshell.remote.stream.SessionOutputStream;
import org.apache.geronimo.gshell.remote.transport.Transport;
import org.apache.geronimo.gshell.remote.transport.TransportCommon;
import org.apache.mina.common.CloseFuture;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;

/**
 * Provides TCP client-side support.
 *
 * @version $Rev$ $Date$
 */
public class TcpTransport
    extends TransportCommon
    implements Transport
{
    private static final int CONNECT_TIMEOUT = 5;

    protected final URI remoteLocation;

    protected final SocketAddress remoteAddress;

    protected final URI localLocation;

    protected final SocketAddress localAddress;

    protected IoConnector connector;

    protected IoSession session;

    protected boolean connected;

    protected TcpTransport(final URI remoteLocation, final SocketAddress remoteAddress, final URI localLocation, final SocketAddress localAddress) throws Exception {
        assert remoteLocation != null;
        assert remoteAddress != null;

        this.remoteLocation = remoteLocation;
        this.remoteAddress = remoteAddress;

        this.localLocation = localLocation;
        this.localAddress = localAddress;
    }

    public TcpTransport(final URI remote, final URI local) throws Exception {
        assert remote != null;
        // local may be null

        this.remoteLocation = remote;
        this.remoteAddress = new InetSocketAddress(InetAddress.getByName(remote.getHost()), remote.getPort());

        if (local != null) {
            this.localLocation = local;
            this.localAddress = new InetSocketAddress(InetAddress.getByName(local.getHost()), local.getPort());
        }
        else {
            // These are final, so make sure to mark them null if we have no local address
            this.localLocation = null;
            this.localAddress = null;
        }
    }

    protected IoConnector createConnector() throws Exception {
        SocketConnector connector = new SocketConnector(Runtime.getRuntime().availableProcessors() + 1, Executors.newCachedThreadPool());

        SocketConnectorConfig config = connector.getDefaultConfig();

        config.getSessionConfig().setKeepAlive(true);

        return connector;
    }

    protected synchronized void init() throws Exception {
        connector = createConnector();

        //
        // HACK: Need to manually wire in the visitor impl for now... :-(
        //

        setMessageVisitor((MessageVisitor) getContainer().lookup(MessageVisitor.class, "client"));
        
        configure(connector);
    }

    public synchronized void connect() throws Exception {
        if (connected) {
            throw new IllegalStateException("Already connected");
        }
        
        init();

        log.info("Connecting to: {}", remoteAddress);

        ConnectFuture cf = connector.connect(remoteAddress, localAddress, getProtocolHandler());

        cf.join();

        session = cf.getSession();

        connected = true;
        
        log.info("Connected");
    }

    public synchronized void close() {
        try {
            CloseFuture cf = session.close();

            cf.join();
        }
        finally {
            super.close();
        }
    }

    public URI getRemoteLocation() {
        return remoteLocation;
    }

    public URI getLocalLocation() {
        return localLocation;
    }

    public WriteFuture send(final Object msg) throws Exception {
        assert msg != null;

        return session.write(msg);
    }

    public Message request(final Message msg) throws Exception {
        assert msg != null;

        Requestor requestor = new Requestor(this);

        return requestor.request(msg);
    }
    
    public Message request(final Message msg, final long timeout, final TimeUnit unit) throws Exception {
        assert msg != null;

        Requestor requestor = new Requestor(this);

        return requestor.request(msg, timeout, unit);
    }
    
    public InputStream getInputStream() {
        return SessionInputStream.lookup(session);
    }

    public OutputStream getOutputStream() {
        return SessionOutputStream.lookup(session);
    }
}