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

import java.net.SocketAddress;
import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.geronimo.gshell.remote.message.MessageVisitor;
import org.apache.geronimo.gshell.remote.security.SecurityFilter;
import org.apache.geronimo.gshell.remote.session.ExecutorThreadModel;
import org.apache.geronimo.gshell.remote.transport.TransportServer;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoAcceptor;

/**
 * Support for {@link TransportServer} implementations.
 *
 * @version $Rev$ $Date$
 */
public abstract class BaseTransportServer
    extends BaseCommon
    implements TransportServer
{
    private static final AtomicLong COUNTER = new AtomicLong(0);

    protected final URI location;

    protected final SocketAddress address;

    protected IoAcceptor acceptor;

    protected ExecutorThreadModel threadModel;

    protected boolean bound;

    protected BaseTransportServer(final URI location, final SocketAddress address) {
        assert location != null;
        assert address != null;

        this.location = location;
        this.address = address;
    }

    public URI getLocation() {
        return location;
    }

    protected abstract IoAcceptor createAcceptor() throws Exception;

    protected synchronized void init() throws Exception {
        acceptor = createAcceptor();

        // Install the thread model
        threadModel = new ExecutorThreadModel(getClass().getSimpleName() + "-" + COUNTER.getAndIncrement());
        acceptor.getDefaultConfig().setThreadModel(threadModel);

        //
        // HACK: Need to manually wire in the visitor impl for now... :-(
        //

        setMessageVisitor((MessageVisitor) getContainer().lookup(MessageVisitor.class, "server"));

        configure(acceptor);
    }

    @Override
    protected void configure(final DefaultIoFilterChainBuilder chain) throws Exception {
        assert chain != null;

        super.configure(chain);

        chain.addLast(SecurityFilter.class.getSimpleName(), getSecurityFilter());
    }

    public synchronized void bind() throws Exception {
        if (bound) {
            throw new IllegalStateException("Already bound");
        }

        init();

        acceptor.bind(address, getHandler());

        bound = true;

        log.info("Listening on: {}", address);
    }

    public boolean isBound() {
        return bound;
    }

    public synchronized void close() {
        try {
            acceptor.unbind(address);

            threadModel.close();
        }
        finally {
            super.close();
        }
    }

    //
    // AutoWire Support, Setters exposed to support Plexus autowire()  Getters exposed to handle state checking.
    //

    //
    // TODO: See if we should tack this puppy on in the handler when the session opens er something? Since this
    //       is rather application specific...
    //
    
    private SecurityFilter securityFilter;

    public void setSecurityFilter(final SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

    protected SecurityFilter getSecurityFilter() {
        if (securityFilter == null) {
            throw new IllegalStateException("Security filter not bound");
        }

        return securityFilter;
    }
}