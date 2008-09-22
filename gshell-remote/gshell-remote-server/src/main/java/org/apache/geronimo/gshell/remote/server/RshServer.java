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

package org.apache.geronimo.gshell.remote.server;

import org.apache.geronimo.gshell.chronos.Duration;
import org.apache.geronimo.gshell.remote.server.handler.ServerMessageHandler;
import org.apache.geronimo.gshell.remote.server.handler.ServerSessionContext;
import org.apache.geronimo.gshell.remote.server.timeout.TimeoutManager;
import org.apache.geronimo.gshell.whisper.message.MessageHandler;
import org.apache.geronimo.gshell.whisper.transport.Session;
import org.apache.geronimo.gshell.whisper.transport.TransportFactory;
import org.apache.geronimo.gshell.whisper.transport.TransportFactoryLocator;
import org.apache.geronimo.gshell.whisper.transport.TransportServer;
import org.apache.mina.common.IoSession;
import org.apache.mina.handler.demux.DemuxingIoHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Provides support for the server-side of the remote shell protocol.
 *
 * @version $Rev$ $Date$
 */
public class RshServer
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private TimeoutManager timeoutManager;

    @Autowired
    private TransportFactoryLocator locator;

    private List<ServerMessageHandler> handlers;

    private TransportServer server;

    public RshServer(final List<ServerMessageHandler> handlers) {
        assert handlers != null;

        this.handlers = handlers;
    }

    public void bind(final URI location) throws Exception {
        TransportFactory factory = locator.locate(location);

        server = factory.bind(location, new Handler());

        log.debug("Bound to: {}", location);
    }

    public void close() {
        server.close();
    }

    public static final Duration AUTH_TIMEOUT = new Duration(10, TimeUnit.SECONDS);

    //
    // IO Handler
    //

    private class Handler
        extends DemuxingIoHandler
    {
        public Handler() throws Exception {
            // Complain if we don't have any handlers
            if (handlers.isEmpty()) {
                throw new Error("No message handlers were discovered");
            }

            for (ServerMessageHandler handler : handlers) {
                register(handler);
            }
        }

        public void register(final MessageHandler handler) {
            assert handler != null;

            Class<?> type = handler.getType();

            log.debug("Registering handler: {} -> {}", type.getSimpleName(), handler);

            // noinspection unchecked
            addMessageHandler(type, handler);
        }

        @Override
        public void messageReceived(final IoSession session, final Object message) throws Exception {
            assert session != null;
            assert message != null;

            //
            // TODO: Insert some security checking here, or really, make a filter to handle that...
            //
            
            super.messageReceived(session, message);
        }
        
        @Override
        public void sessionOpened(final IoSession session) throws Exception {
            assert session != null;

            final Session s = Session.BINDER.lookup(session);

            // Install the session context
            ServerSessionContext context = ServerSessionContext.BINDER.bind(session, new ServerSessionContext());
            log.debug("Created session context: {}", context);
            
            // Schedule a task to timeout the handshake process
            timeoutManager.scheduleTimeout(s, AUTH_TIMEOUT, new Runnable() {
                public void run() {
                    log.error("Timeout waiting for handshake from: {}", s);

                    session.close();
                }
            });
        }

        @Override
        public void sessionClosed(final IoSession session) throws Exception {
            assert session != null;

            ServerSessionContext context = ServerSessionContext.BINDER.unbind(session);
            log.debug("Removed session context: {}", context);
        }
    }
}