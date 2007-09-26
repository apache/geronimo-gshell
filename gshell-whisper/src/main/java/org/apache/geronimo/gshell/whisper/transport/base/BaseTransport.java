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

package org.apache.geronimo.gshell.whisper.transport.base;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;
import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.geronimo.gshell.common.Duration;
import org.apache.geronimo.gshell.whisper.message.Message;
import org.apache.geronimo.gshell.whisper.request.Requestor;
import org.apache.geronimo.gshell.whisper.session.SessionAttributeBinder;
import org.apache.geronimo.gshell.whisper.stream.SessionInputStream;
import org.apache.geronimo.gshell.whisper.stream.SessionOutputStream;
import org.apache.geronimo.gshell.whisper.transport.Transport;
import org.apache.mina.common.CloseFuture;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;

/**
 * Support for {@link Transport} implementations.
 *
 * @version $Rev$ $Date$
 */
public abstract class BaseTransport<T extends IoConnector>
    extends BaseService
    implements Transport
{
    private static final AtomicLong INSTANCE_COUNTER = new AtomicLong(0);

    private static final SessionAttributeBinder<Transport> TRANSPORT = new SessionAttributeBinder<Transport>(Transport.class);

    protected URI remoteLocation;

    protected SocketAddress remoteAddress;

    protected URI localLocation;

    protected SocketAddress localAddress;

    protected T connector;

    protected IoSession session;

    protected BaseTransport(final AddressFactory addressFactory) {
        super(addressFactory);
    }

    //
    // Configuration
    //

    protected static class BaseTransportConfiguration
        extends BaseConfiguration
        implements Transport.Configuration
    {
        // TODO:
    }

    private Configuration config;

    protected abstract Configuration createConfiguration();

    public synchronized Configuration getConfiguration() {
        if (config == null) {
            config = createConfiguration();
        }

        return config;
    }

    public synchronized void setConfiguration(final Configuration config) {
        assert config != null;

        this.config = config;

        log.debug("Using configuration: {}", config);
    }

    @Override
    protected synchronized BaseConfiguration getBaseConfiguration() {
        return (BaseConfiguration) getConfiguration();
    }

    //
    // Connection
    //

    protected abstract T createConnector() throws Exception;

    synchronized void connect(final URI remote, final URI local) throws Exception {
        this.remoteLocation = remote;
        this.remoteAddress = addressFactory.create(remote);

        this.localLocation = local;
        this.localAddress = addressFactory.create(local);

        connect();
    }

    synchronized void connect() throws Exception {
        log.debug("Connecting");

        connector = createConnector();
        log.debug("Connector: {}", connector);

        configure(connector);

        IoHandler handler = getHandler();
        log.debug("Handler: {}", handler);

        log.info("Connecting to: {}", remoteAddress);
        ConnectFuture cf = connector.connect(remoteAddress, localAddress, handler);

        log.debug("Waiting for connection to establish");
        cf.join();

        // And fetch session so we can talk
        session = cf.getSession();
        log.debug("Session: ", session);

        // Maybe configure something
        configure(session);

        // Stuff the transport instance into the session's context so we can find ourself later
        TRANSPORT.bind(session, this);

        log.info("Connected to: {}", session.getRemoteAddress());
    }

    public synchronized void close() {
        if (isClosed()) {
            // Ignore duplciate close
            return;
        }

        try {
            TRANSPORT.unbind(session);
            
            CloseFuture cf = session.close();
            cf.join();
        }
        finally {
            super.close();
        }
    }

    public URI getRemote() {
        return remoteLocation;
    }

    public URI getLocal() {
        return localLocation;
    }

    public T getConnector() {
        return connector;
    }

    public IoSession getSession() {
        return session;
    }

    //
    // Streams
    //

    public InputStream getInputStream() {
        return SessionInputStream.BINDER.lookup(session);
    }

    public OutputStream getOutputStream() {
        return SessionOutputStream.BINDER.lookup(session);
    }

    public OutputStream getErrorStream() {
        throw new UnsupportedOperationException("TODO");
    }

    //
    // Sending Messages
    //

    public WriteFuture send(final Object msg) throws Exception {
        assert msg != null;

        return session.write(msg);
    }

    public Message request(final Message msg) throws Exception {
        assert msg != null;

        Requestor requestor = new Requestor(this);

        return requestor.request(msg);
    }

    public Message request(final Message msg, final Duration timeout) throws Exception {
        assert msg != null;
        assert timeout != null;

        Requestor requestor = new Requestor(this);

        return requestor.request(msg, timeout);
    }

    //
    // Listeners
    //

    public void addListener(final Listener listener) {
        assert listener != null;

        throw new UnsupportedOperationException();
    }

    public void removeListener(final Listener listener) {
        assert listener != null;

        throw new UnsupportedOperationException();
    }
}