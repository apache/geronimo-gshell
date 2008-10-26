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

import org.apache.geronimo.gshell.whisper.transport.Session;
import org.apache.geronimo.gshell.whisper.transport.Transport;
import org.apache.geronimo.gshell.whisper.util.SessionAttributeBinder;
import org.apache.mina.common.CloseFuture;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;

import java.net.SocketAddress;
import java.net.URI;

/**
 * Support for {@link Transport} implementations.
 *
 * @version $Rev$ $Date$
 */
public abstract class BaseTransport<T extends IoConnector>
    extends BaseService
    implements Transport
{
    private static final SessionAttributeBinder<Transport> TRANSPORT = new SessionAttributeBinder<Transport>(Transport.class);

    protected URI remoteLocation;

    protected SocketAddress remoteAddress;

    protected URI localLocation;

    protected SocketAddress localAddress;

    protected T connector;

    protected Session session;

    protected BaseTransport(final AddressFactory addressFactory) {
        super(addressFactory);
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
        IoSession s = cf.getSession();
        session = new SessionAdapter(s);

        log.debug("Session: ", session);

        // Maybe configure something
        configure(s);

        // Stuff the transport instance into the session's context so we can find ourself later
        TRANSPORT.bind(s, this);

        log.info("Connected to: {}", s.getRemoteAddress());
    }

    public synchronized void close() {
        if (isClosed()) {
            // Ignore duplciate close
            return;
        }

        try {
            IoSession s = session.getSession();

            TRANSPORT.unbind(s);
            
            CloseFuture cf = s.close();
            
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

    public Session getSession() {
        return session;
    }
}