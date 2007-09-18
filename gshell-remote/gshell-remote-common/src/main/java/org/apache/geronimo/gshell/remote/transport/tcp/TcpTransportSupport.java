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

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.remote.logging.LoggingFilter;
import org.apache.geronimo.gshell.remote.message.MessageCodecFactory;
import org.apache.geronimo.gshell.remote.message.MessageVisitor;
import org.apache.geronimo.gshell.remote.transport.Transport;
import org.apache.geronimo.gshell.remote.transport.TransportServer;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoService;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.reqres.RequestResponseFilter;
import org.codehaus.plexus.PlexusContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for TCP {@link Transport} and {@link TransportServer} instances.
 *
 * @version $Rev$ $Date$
 */
public class TcpTransportSupport
{
    public static final String PROTOCOL_FILTER_NAME = "protocol";

    public static final String REQRESP_FILTER_NAME = "reqresp";

    protected Logger log = LoggerFactory.getLogger(getClass());

    private PlexusContainer container;

    private MessageVisitor messageVisitor;

    private TcpProtocolHandler protocolHandler;

    private MessageCodecFactory codecFactory;

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    protected void configure(final IoService service) throws Exception {
        assert service != null;

        TcpProtocolHandler handler = getProtocolHandler();
        MessageVisitor visitor = getMessageVisitor();
        handler.setVisitor(visitor);
        service.setHandler(handler);

        DefaultIoFilterChainBuilder filterChain = service.getFilterChain();
        
        if (log.isDebugEnabled()) {
            filterChain.addLast(LoggingFilter.NAME, new LoggingFilter());
        }

        filterChain.addLast(PROTOCOL_FILTER_NAME, new ProtocolCodecFilter(getMessageCodecFactory()));

        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());

        filterChain.addLast(REQRESP_FILTER_NAME, new RequestResponseFilter(handler.getResponseInspector(), scheduler));
    }

    //
    // NOTE: Setters exposed to support Plexus autowire()  Getters exposed to handle state checking.
    //

    public void setMessageVisitor(final MessageVisitor messageVisitor) {
        assert messageVisitor != null;

        log.debug("Using message visitor: {}", messageVisitor);

        this.messageVisitor = messageVisitor;
    }

    protected MessageVisitor getMessageVisitor() {
        if (messageVisitor == null) {
            throw new IllegalStateException("Message visitor not bound");
        }

        return messageVisitor;
    }

    public void setProtocolHandler(final TcpProtocolHandler protocolHandler) {
        assert protocolHandler != null;

        log.debug("Using protocol handler: {}", protocolHandler);

        this.protocolHandler = protocolHandler;
    }

    protected TcpProtocolHandler getProtocolHandler() {
        if (protocolHandler == null) {
            throw new IllegalStateException("Protocol handler not bound");
        }

        return protocolHandler;
    }

    public void setMessageCodecFactory(final MessageCodecFactory codecFactory) {
        assert codecFactory != null;
        
        log.debug("Using codec factory: {}", codecFactory);

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
        log.debug("Using plexus container: {}", container);

        this.container = container;
    }

    protected PlexusContainer getContainer() {
        if (container == null) {
            throw new IllegalStateException("Container not bound");
        }

        return container;
    }
}