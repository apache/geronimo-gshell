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

import java.net.URI;
import java.util.concurrent.Executors;

import org.apache.geronimo.gshell.remote.transport.base.BaseTransport;
import org.apache.mina.common.IoConnector;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;

/**
 * Provides TCP client-side support.
 *
 * @version $Rev$ $Date$
 */
public class TcpTransport
    extends BaseTransport
{
    public TcpTransport(final URI remote, final URI local) throws Exception {
        super(remote, TcpTransportFactory.address(remote), local, TcpTransportFactory.address(local));
    }

    @Override
    protected IoConnector createConnector() throws Exception {
        SocketConnector connector = new SocketConnector(Runtime.getRuntime().availableProcessors() + 1, Executors.newCachedThreadPool());

        SocketConnectorConfig config = connector.getDefaultConfig();

        config.getSessionConfig().setKeepAlive(true);

        return connector;
    }
}