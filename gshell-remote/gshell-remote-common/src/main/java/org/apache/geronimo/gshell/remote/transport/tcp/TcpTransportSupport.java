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
import org.apache.geronimo.gshell.remote.filter.LoggingFilter;
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
    protected Logger log = LoggerFactory.getLogger(getClass());

    private PlexusContainer container;

    private MessageVisitor messageVisitor;

    private TcpProtocolHandler protocolHandler;

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
            filterChain.addLast("logging", new LoggingFilter());
        }

        filterChain.addLast("protocol", new ProtocolCodecFilter(new MessageCodecFactory()));

        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());

        filterChain.addLast("reqresp", new RequestResponseFilter(protocolHandler.getResponseInspector(), scheduler));
    }

    //
    // NOTE: Setters exposed to support Plexus autowire()  Getters exposed to handle state checking.
    //

    public void setMessageVisitor(final MessageVisitor messageVisitor) {
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
        log.debug("Using protocol handler: {}", protocolHandler);

        this.protocolHandler = protocolHandler;
    }

    protected TcpProtocolHandler getProtocolHandler() {
        if (protocolHandler == null) {
            throw new IllegalStateException("Protocol handler not bound");
        }

        return protocolHandler;
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