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

package org.apache.geronimo.gshell.remote.transport;

import java.net.SocketAddress;

import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.remote.codec.MessageCodecFactory;
import org.apache.geronimo.gshell.remote.message.MessageVisitor;
import org.apache.geronimo.gshell.remote.request.RequestResponseFilter;
import org.apache.geronimo.gshell.remote.stream.SessionStreamFilter;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoFilterChain;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoService;
import org.apache.mina.common.IoServiceConfig;
import org.apache.mina.common.IoServiceListener;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.management.IoSessionStat;
import org.apache.mina.management.StatCollector;
import org.codehaus.plexus.PlexusContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common bits for {@link Transport} and {@link TransportServer} implementations.
 *
 * @version $Rev$ $Date$
 */
public abstract class TransportCommon
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    // private StatCollector statCollector;

    private IoService service;

    protected TransportCommon() {
        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    protected void configure(final IoService service) throws Exception {
        assert service != null;

        this.service = service;

        service.addListener(new IoServiceListener() {
            public void serviceActivated(IoService service, SocketAddress serviceAddress, IoHandler handler, IoServiceConfig config) {
                log.info("Service activated: {}", service);

                // log.info("Service activated: {}, {}, {}, {}", service, serviceAddress, handler, config);

                logFilters(service);
            }

            public void serviceDeactivated(IoService service, SocketAddress serviceAddress, IoHandler handler, IoServiceConfig config) {
                log.info("Service deactivated: {}", service);

                // log.info("Service deactivated: {}, {}, {}, {}", service, serviceAddress, handler, config);
            }

            public void sessionCreated(IoSession session) {
                log.info("Session created: {}", session);

                logFilters(session);
            }

            public void sessionDestroyed(IoSession session) {
                log.info("Session destroyed: {}", session);
            }
        });

        ProtocolHandler handler = getProtocolHandler();

        MessageVisitor visitor = getMessageVisitor();

        handler.setVisitor(visitor);

        // Install the default set of filters

        DefaultIoFilterChainBuilder filterChain = service.getFilterChain();

        //
        // NOTE: We don't need an executor filter here, since the ThreadModel does that for us
        //
        
        // filterChain.addLast(ExecutorFilter.class.getSimpleName(), new ExecutorFilter(executor));

        // filterChain.addLast(ProfilerTimerFilter.class.getSimpleName(), new ProfilerTimerFilter());

        filterChain.addLast(ProtocolCodecFilter.class.getSimpleName(), new ProtocolCodecFilter(getMessageCodecFactory()));

        filterChain.addLast(LoggingFilter.class.getSimpleName(), new LoggingFilter());

        filterChain.addLast(SessionStreamFilter.class.getSimpleName(), new SessionStreamFilter());

        filterChain.addLast(RequestResponseFilter.class.getSimpleName(), new RequestResponseFilter());

        //
        // TODO: Start up a scheduled task to periodically log stats
        //

        // Setup stat collection
        // statCollector = new StatCollector(service);
        // statCollector.start();
    }

    public IoService getService() {
        return service;
    }

    public void close() {
        // statCollector.stop();
    }

    //
    // Logging Helpers
    //
    
    protected void logFilters(IoService service) {
        DefaultIoFilterChainBuilder filterChain = service.getFilterChain();

        log.debug("Service filters:");

        for (IoFilterChain.Entry entry : filterChain.getAll()) {
            log.debug("    {}", entry);
        }
    }

    protected void logFilters(IoSession session) {
        IoFilterChain filterChain = session.getFilterChain();

        log.debug("Session filters:");

        for (IoFilterChain.Entry entry : filterChain.getAll()) {
            log.debug("    {}", entry);
        }
    }

    protected void logStats(final IoSession session) throws Exception {
        assert session != null;

        IoSessionStat stat = (IoSessionStat) session.getAttribute(StatCollector.KEY);

        if (stat != null) {
            log.debug("Stats: {}", ReflectionToStringBuilder.toString(stat, ToStringStyle.SHORT_PREFIX_STYLE));
        }
    }

    //
    // AutoWire Support
    //

    private PlexusContainer container;

    private MessageVisitor messageVisitor;

    private ProtocolHandler protocolHandler;

    private MessageCodecFactory codecFactory;

    //
    // NOTE: Setters exposed to support Plexus autowire()  Getters exposed to handle state checking.
    //

    public void setMessageVisitor(final MessageVisitor messageVisitor) {
        assert messageVisitor != null;

        log.trace("Using message visitor: {}", messageVisitor);

        this.messageVisitor = messageVisitor;
    }

    protected MessageVisitor getMessageVisitor() {
        if (messageVisitor == null) {
            throw new IllegalStateException("Message visitor not bound");
        }

        return messageVisitor;
    }

    public void setProtocolHandler(final ProtocolHandler protocolHandler) {
        assert protocolHandler != null;

        log.trace("Using protocol handler: {}", protocolHandler);

        this.protocolHandler = protocolHandler;
    }

    protected ProtocolHandler getProtocolHandler() {
        if (protocolHandler == null) {
            throw new IllegalStateException("Protocol handler not bound");
        }

        return protocolHandler;
    }

    public void setMessageCodecFactory(final MessageCodecFactory codecFactory) {
        assert codecFactory != null;

        log.trace("Using codec factory: {}", codecFactory);

        this.codecFactory = codecFactory;
    }

    protected MessageCodecFactory getMessageCodecFactory() {
        if (codecFactory == null) {
            throw new IllegalStateException("Message codec factory not bound");
        }

        return codecFactory;
    }

    //
    // HACK: Added for manual wiring of the message visitor, autowire() can't seem to handle it... :-(
    //

    public void setContainer(final PlexusContainer container) {
        log.trace("Using plexus container: {}", container);

        this.container = container;
    }

    protected PlexusContainer getContainer() {
        if (container == null) {
            throw new IllegalStateException("Container not bound");
        }

        return container;
    }
}