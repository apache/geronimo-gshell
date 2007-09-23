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
import org.apache.geronimo.gshell.whisper.message.MessageHandler;
import org.apache.geronimo.gshell.whisper.message.spi.MessageProvider;
import org.apache.geronimo.gshell.whisper.request.RequestResponseFilter;
import org.apache.geronimo.gshell.whisper.stream.SessionStreamFilter;
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
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.codehaus.plexus.PlexusContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common bits for {@link Transport} and {@link TransportServer} implementations.
 *
 * @version $Rev$ $Date$
 */
public abstract class BaseCommon
{
    static {
        // Setup our exception monitor
        ExceptionMonitor.setInstance(new TransportExceptionMonitor());
        
        // Make sure that we use non-pooled fast buffers
        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
    }

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private IoHandler handler;

    private IoService service;

    // private StatCollector statCollector;

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    protected IoHandler getHandler() {
        if (handler == null) {
            throw new IllegalStateException("Handler not bound");
        }
        
        return handler;
    }

    public IoService getService() {
        return service;
    }

    //
    // Configuration
    //
    
    protected void configure(final IoService service) throws Exception {
        assert service != null;

        this.service = service;

        service.addListener(new IoServiceListener() {
            public void serviceActivated(final IoService service, final SocketAddress serviceAddress, final IoHandler handler, final IoServiceConfig config) {
                log.info("Service activated: {}", service);

                // log.info("Service activated: {}, {}, {}, {}", service, serviceAddress, handler, config);

                logFilters(service);
            }

            public void serviceDeactivated(final IoService service, final SocketAddress serviceAddress, final IoHandler handler, final IoServiceConfig config) {
                log.info("Service deactivated: {}", service);

                // log.info("Service deactivated: {}, {}, {}, {}", service, serviceAddress, handler, config);
            }

            public void sessionCreated(final IoSession session) {
                log.info("Session created: {}", session);

                logFilters(session);
            }

            public void sessionDestroyed(final IoSession session) {
                log.info("Session destroyed: {}", session);
            }
        });

        // Setup the io handler
        handler = getMessageHandler();

        // Install the default set of filters
        configure(service.getFilterChain());

        //
        // TODO: Start up a scheduled task to periodically log stats
        //

        // Setup stat collection
        // statCollector = new StatCollector(service);
        // statCollector.start();
    }

    protected void configure(final DefaultIoFilterChainBuilder chain) throws Exception {
        assert chain != null;

        // chain.addLast(ProfilerTimerFilter.class.getSimpleName(), new ProfilerTimerFilter());

        //
        // TODO: Test this guy out....
        //
        // chain.addLast(CryptoContextInjectingFilter.class.getSimpleName(), new CryptoContextInjectingFilter(getProvider().getCryptoContext()));

        chain.addLast(ProtocolCodecFilter.class.getSimpleName(), new ProtocolCodecFilter(getProvider().getProtocolCodecFactory()));
        
        chain.addLast(LoggingFilter.class.getSimpleName(), new LoggingFilter());

        chain.addLast(SessionStreamFilter.class.getSimpleName(), new SessionStreamFilter());

        chain.addLast(RequestResponseFilter.class.getSimpleName(), new RequestResponseFilter());
    }

    public void close() {
        // statCollector.stop();
    }

    //
    // Logging Helpers
    //
    
    protected void logFilters(final IoService service) {
        assert service != null;

        log.debug("Service filters:");

        for (IoFilterChain.Entry entry : service.getFilterChain().getAll()) {
            log.debug("    {}", entry);
        }
    }

    protected void logFilters(final IoSession session) {
        assert session != null;
        
        log.debug("Session filters:");

        for (IoFilterChain.Entry entry : session.getFilterChain().getAll()) {
            log.debug("    {}", entry);
        }
    }

    /*
    protected void logStats(final IoSession session) {
        assert session != null;

        IoSessionStat stat = (IoSessionStat) session.getAttribute(StatCollector.KEY);

        if (stat != null) {
            log.debug("Stats: {}", ReflectionToStringBuilder.toString(stat, ToStringStyle.SHORT_PREFIX_STYLE));
        }
    }
    */
    
    //
    // AutoWire Support, Setters exposed to support Plexus autowire()  Getters exposed to handle state checking.
    //

    private PlexusContainer container;

    private MessageProvider provider;

    private MessageHandler messageHandler;

    public void setMessageHandler(final MessageHandler messageHandler) {
        assert messageHandler != null;

        log.trace("Using message handler: {}", messageHandler);

        this.messageHandler = messageHandler;
    }

    protected MessageHandler getMessageHandler() {
        if (messageHandler == null) {
            throw new IllegalStateException("Message handler not bound");
        }

        return messageHandler;
    }
    
    public void setProvider(final MessageProvider provider) {
        assert provider != null;

        log.trace("Using provider: {}", provider);

        this.provider = provider;
    }

    protected MessageProvider getProvider() {
        if (provider == null) {
            throw new IllegalStateException("Provider not bound");
        }

        return provider;
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