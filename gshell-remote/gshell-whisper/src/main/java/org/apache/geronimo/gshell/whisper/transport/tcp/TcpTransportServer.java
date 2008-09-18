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

package org.apache.geronimo.gshell.whisper.transport.tcp;

import java.util.concurrent.Executors;

import org.apache.geronimo.gshell.whisper.transport.TransportServer;
import org.apache.geronimo.gshell.whisper.transport.base.AddressFactory;
import org.apache.geronimo.gshell.whisper.transport.base.BaseTransportServer;
import org.apache.mina.transport.socket.nio.SocketAcceptor;

/**
 * Provides TCP server-side support.
 *
 * @version $Rev$ $Date$
 */
public class TcpTransportServer
    extends BaseTransportServer
{
    public TcpTransportServer() {
        super(new TcpAddressFactory());
    }

    protected TcpTransportServer(final AddressFactory addressFactory) {
        super(addressFactory);
    }

    @Override
    protected SocketAcceptor createAcceptor() throws Exception {
        return new SocketAcceptor(Runtime.getRuntime().availableProcessors() + 1, Executors.newCachedThreadPool());
    }

    protected TransportServer.Configuration createConfiguration() {
        return new Configuration();
    }

    public static class Configuration
        extends BaseTransportServerConfiguration
    {
        // TODO:
    }
}