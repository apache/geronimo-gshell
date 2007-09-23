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

package org.apache.geronimo.gshell.remote.transport.base;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.geronimo.gshell.common.Duration;
import org.apache.geronimo.gshell.remote.message.Message;
import org.apache.geronimo.gshell.remote.message.MessageHandler;
import org.apache.geronimo.gshell.remote.message.MessageVisitor;
import org.apache.geronimo.gshell.remote.request.Requestor;
import org.apache.geronimo.gshell.remote.session.ThreadPoolModel;
import org.apache.geronimo.gshell.remote.stream.SessionInputStream;
import org.apache.geronimo.gshell.remote.stream.SessionOutputStream;
import org.apache.geronimo.gshell.remote.transport.Transport;
import org.apache.mina.common.CloseFuture;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Support for {@link Transport} implementations.
 *
 * @version $Rev$ $Date$
 */
public abstract class BaseTransport
    extends BaseCommon
    implements Transport
{
    private static final AtomicLong COUNTER = new AtomicLong(0);
    
    protected final URI remoteLocation;

    protected final SocketAddress remoteAddress;

    protected final URI localLocation;

    protected final SocketAddress localAddress;

    protected IoConnector connector;

    protected ThreadPoolModel threadModel;

    protected IoSession session;

    protected boolean connected;

    @Requirement(role=MessageVisitor.class, hint="client")
    private MessageVisitor v;

    protected BaseTransport(final URI remoteLocation, final SocketAddress remoteAddress, final URI localLocation, final SocketAddress localAddress) throws Exception {
        assert remoteLocation != null;
        assert remoteAddress != null;

        this.remoteLocation = remoteLocation;
        this.remoteAddress = remoteAddress;

        this.localLocation = localLocation;
        this.localAddress = localAddress;
    }

    protected abstract IoConnector createConnector() throws Exception;

    protected synchronized void init() throws Exception {
        // For now we must manually bind the message handler, plexus is unable to provide injection for us
        setMessageHandler((MessageHandler) getContainer().lookup(MessageHandler.class, "client"));

        // Setup the connector service
        connector = createConnector();

        // Install the thread model
        threadModel = new ThreadPoolModel(getClass().getSimpleName() + "-" + COUNTER.getAndIncrement());
        connector.getDefaultConfig().setThreadModel(threadModel);

        // Configure the connector
        configure(connector);
    }

    public synchronized void connect() throws Exception {
        if (connected) {
            throw new IllegalStateException("Already connected");
        }

        init();

        log.info("Connecting to: {}", remoteAddress);

        ConnectFuture cf = connector.connect(remoteAddress, localAddress, getHandler());

        cf.join();

        session = cf.getSession();

        connected = true;

        log.info("Connected");
    }

    public boolean isConnected() {
        return connected;
    }

    public synchronized void close() {
        try {
            CloseFuture cf = session.close();

            cf.join();

            threadModel.close();
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

    public IoSession getSession() {
        return session;
    }

    public InputStream getInputStream() {
        return SessionInputStream.BINDER.lookup(session);
    }

    public OutputStream getOutputStream() {
        return SessionOutputStream.BINDER.lookup(session);
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

    public Message request(final Message msg, final Duration timeout) throws Exception {
        assert msg != null;

        Requestor requestor = new Requestor(this);

        return requestor.request(msg, timeout);
    }

    public Message request(final Message msg, final long timeout, final TimeUnit unit) throws Exception {
        assert msg != null;

        Requestor requestor = new Requestor(this);

        return requestor.request(msg, timeout, unit);
    }
}