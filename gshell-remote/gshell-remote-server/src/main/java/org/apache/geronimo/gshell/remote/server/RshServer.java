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

import java.net.InetSocketAddress;

import org.apache.geronimo.gshell.remote.message.codec.MessageCodecFactory;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
@Component(role=RshServer.class)
public class RshServer
{
    private Logger log = LoggerFactory.getLogger(getClass());

    @Requirement(role=IoHandler.class, hint="rsh-server")
    private RshServerProtocolHandler handler;

    private SocketAcceptor acceptor;

    private boolean bound = false;

    public void bind(final int port) throws Exception {
        if (bound) {
            throw new IllegalStateException("Already bound");
        }

        handler.setVisitor(new RshServerMessageVisitor());

        acceptor = new SocketAcceptor();
        acceptor.setLocalAddress(new InetSocketAddress(port));
        acceptor.setHandler(handler);

        DefaultIoFilterChainBuilder filterChain = acceptor.getFilterChain();
        filterChain.addLast("logger", new LoggingFilter());
        filterChain.addLast("protocol", new ProtocolCodecFilter(new MessageCodecFactory()));

        acceptor.bind();

        log.info("Listening on port: {}", port);

        bound = true;
    }
}