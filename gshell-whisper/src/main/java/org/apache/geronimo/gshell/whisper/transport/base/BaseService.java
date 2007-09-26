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

import java.net.SocketAddress;

import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.whisper.transport.Transport;
import org.apache.geronimo.gshell.whisper.transport.TransportExceptionMonitor;
import org.apache.geronimo.gshell.whisper.transport.TransportServer;
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
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common bits for {@link Transport} and {@link TransportServer} implementations.
 *
 * @version $Rev$ $Date$
 */
public abstract class BaseService<T extends IoService>
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
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    //
    // Configuration
    //

    protected abstract BaseConfiguration getBaseConfiguration();

    private IoHandler handler;

    protected synchronized IoHandler createHandler() throws Exception {
        return getBaseConfiguration().getHandler();
    }

    protected synchronized IoHandler getHandler() throws Exception {
        if (handler == null) {
            handler = createHandler();
        }

        if (handler == null) {
            throw new IllegalStateException("Handler has not been configured");
        }
        
        return handler;
    }

    private ThreadModel threadModel;

    protected synchronized ThreadModel createThreadModel() throws Exception {
        return getBaseConfiguration().getThreadModel();
    }

    protected synchronized ThreadModel getThreadModel() throws Exception {
        if (threadModel == null) {
            threadModel = createThreadModel();
        }

        // This can be null to leave the default model installed
        return threadModel;
    }

    protected void configure(final IoService service) throws Exception {
        assert service != null;

        log.debug("Configure: {}", service);
        
        // Watch for service events
        service.addListener(new IoServiceListener() {
            private void log(Object msg) {
                System.err.println(">>> [" + Thread.currentThread().getName() + "] " + msg);
            }

            public void serviceActivated(final IoService service, final SocketAddress serviceAddress, final IoHandler handler, final IoServiceConfig config) {
                log("Service activated: " + service);

                log.debug("Service activated: {}, filters:", service);

                for (IoFilterChain.Entry entry : service.getFilterChain().getAll()) {
                    log.debug("    {}", entry);
                }
            }

            public void serviceDeactivated(final IoService service, final SocketAddress serviceAddress, final IoHandler handler, final IoServiceConfig config) {
                log("Service deactivated: " + service);

                log.debug("Service deactivated: {}", service);
            }

            public void sessionCreated(final IoSession session) {
                log("Session created: " + service);

                log.debug("Session created: {}; filters:", session);

                for (IoFilterChain.Entry entry : session.getFilterChain().getAll()) {
                    log.debug("    {}", entry);
                }
            }

            public void sessionDestroyed(final IoSession session) {
                log("Session destroyed: " + service);
                
                log.debug("Session destroyed: {}", session);
            }
        });


        configure(service.getDefaultConfig());
        
        configure(service.getFilterChain());
    }

    protected void configure(final IoServiceConfig config) throws Exception {
        assert config != null;

        log.debug("Configure: {}", config);

        ThreadModel threadModel = getThreadModel();

        if (threadModel != null) {
            config.setThreadModel(threadModel);
            log.debug("Installed custom thread model: {}", threadModel);
        }
    }

    protected void configure(final DefaultIoFilterChainBuilder chain) throws Exception {
        assert chain != null;

        log.debug("Configure: {}", chain);

        // For right now just add a few hard codded to test with

        chain.addLast(ProtocolCodecFilter.class.getSimpleName(), new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));

        chain.addLast(LoggingFilter.class.getSimpleName(), new LoggingFilter());
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