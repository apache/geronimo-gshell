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

package org.apache.geronimo.gshell.remote.transport.vm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.apache.geronimo.gshell.remote.message.Message;
import org.apache.geronimo.gshell.remote.message.MessageVisitor;
import org.apache.geronimo.gshell.remote.transport.Transport;
import org.apache.geronimo.gshell.remote.transport.TransportSupport;
import org.apache.mina.common.CloseFuture;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.filter.reqres.Request;
import org.apache.mina.filter.reqres.Response;
import org.apache.mina.transport.vmpipe.VmPipeAddress;
import org.apache.mina.transport.vmpipe.VmPipeConnector;

/**
 * Provides in-VM client-side support.
 *
 * @version $Rev$ $Date$
 */
public class VmTransport
    extends TransportSupport
    implements Transport
{
    private static final int CONNECT_TIMEOUT = 3000;
    
    protected final URI remoteLocation;

    protected final VmPipeAddress remoteAddress;

    protected final URI localLocation;

    protected final VmPipeAddress localAddress;

    protected VmPipeConnector connector;

    protected IoSession session;

    protected boolean connected;

    public VmTransport(final URI remote, final URI local) throws Exception {
        assert remote != null;
        // local may be null

        this.remoteLocation = remote;
        this.remoteAddress = new VmPipeAddress(remote.getPort());

        if (local != null) {
            this.localLocation = local;
            this.localAddress = new VmPipeAddress(local.getPort());
        }
        else {
            // These are final, so make sure to mark them null if we have no local address
            this.localLocation = null;
            this.localAddress = null;
        }
    }

    protected synchronized void init() throws Exception {
        connector = new VmPipeConnector();

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

    public synchronized void close() {
        CloseFuture cf = session.close();

        cf.awaitUninterruptibly();

        log.info("Closed");
    }

    public URI getRemoteLocation() {
        return remoteLocation;
    }

    public URI getLocalLocation() {
        return localLocation;
    }

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
        return request(msg, 5, TimeUnit.SECONDS);
    }

    public Message request(final Message msg, final long timeout, final TimeUnit unit) throws Exception {
        assert msg != null;

        Request req = new Request(msg.getId(), msg, timeout, unit);

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
}