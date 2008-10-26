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

import org.apache.geronimo.gshell.whisper.request.RequestResponseFilter;
import org.apache.geronimo.gshell.whisper.stream.SessionStreamFilter;
import org.apache.geronimo.gshell.whisper.transport.Transport;
import org.apache.geronimo.gshell.whisper.transport.TransportExceptionMonitor;
import org.apache.geronimo.gshell.whisper.transport.TransportServer;
import org.apache.geronimo.gshell.yarn.Yarn;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.ExceptionMonitor;
import org.apache.mina.common.IoFilterChain;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoService;
import org.apache.mina.common.IoServiceConfig;
import org.apache.mina.common.IoServiceListener;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.Executors;

/**
 * Common bits for {@link Transport} and {@link TransportServer} implementations.
 *
 * @version $Rev$ $Date$
 */
public abstract class BaseService
{
    static {
        // Setup our exception monitor
        ExceptionMonitor.setInstance(new TransportExceptionMonitor());
        
        // Make sure that we use non-pooled fast buffers
        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
    }

    protected transient final Logger log = LoggerFactory.getLogger(getClass());

    protected AddressFactory addressFactory;

    protected BaseService(final AddressFactory addressFactory) {
        this.addressFactory = addressFactory;
    }

    public String toString() {
        return Yarn.render(this);
    }

    private IoHandler handler;

    public synchronized void setHandler(final IoHandler handler) {
        assert handler != null;
        this.handler = handler;
    }

    protected synchronized IoHandler getHandler() throws Exception {
        if (handler == null) {
            throw new IllegalStateException("Handler has not been configured");
        }
        
        return handler;
    }

    protected void configure(final IoService service) throws Exception {
        assert service != null;
        
        // Watch for service events
        service.addListener(new IoServiceListener() {
            public void serviceActivated(final IoService service, final SocketAddress serviceAddress, final IoHandler handler, final IoServiceConfig config) {
                log.debug("Service activated: {}, filters:", service);

                for (IoFilterChain.Entry entry : service.getFilterChain().getAll()) {
                    log.debug("    {} -> {}", entry.getName(), entry.getFilter());
                }
            }

            public void serviceDeactivated(final IoService service, final SocketAddress serviceAddress, final IoHandler handler, final IoServiceConfig config) {
                log.debug("Service deactivated: {}", service);
            }

            public void sessionCreated(final IoSession session) {
                log.debug("Session created: {}; filters:", session);

                for (IoFilterChain.Entry entry : session.getFilterChain().getAll()) {
                    log.debug("    {} -> {}", entry.getName(), entry.getFilter());
                }
            }

            public void sessionDestroyed(final IoSession session) {
                log.debug("Session destroyed: {}", session);
            }
        });


        service.getDefaultConfig().setThreadModel(ThreadModel.MANUAL);
        
        configure(service.getFilterChain());
    }

    protected void configure(final DefaultIoFilterChainBuilder chain) throws Exception {
        assert chain != null;

        //
        // HACK: For right now just add a few hard codded to test with, need to make all this spring configured
        //

        chain.addLast(SessionBindingFilter.class.getSimpleName(), new SessionBindingFilter());

        chain.addLast(ProtocolCodecFilter.class.getSimpleName(), new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));

        chain.addLast(ExecutorFilter.class.getSimpleName(), new ExecutorFilter(Executors.newCachedThreadPool()));

        chain.addLast(LoggingFilter.class.getSimpleName(), new LoggingFilter());

        chain.addLast(SessionStreamFilter.class.getSimpleName(), new SessionStreamFilter());

        chain.addLast(RequestResponseFilter.class.getSimpleName(), new RequestResponseFilter());
    }

    protected void configure(final IoSession session) throws Exception {
        assert session != null;

        log.debug("Configure: {}", session);
    }

    //
    // Closeable
    //

    private boolean closed;

    public synchronized boolean isClosed() {
        return closed;
    }

    protected void ensureOpened() {
        if (isClosed()) {
            throw new IllegalStateException("Closed");
        }
    }

    public synchronized void close() {
        if (isClosed()) {
            // ignore duplicate close
            return;
        }

        closed = true;
    }
}