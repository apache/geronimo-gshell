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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;

import org.apache.geronimo.gshell.remote.transport.Transport;
import org.apache.geronimo.gshell.remote.transport.TransportFactory;
import org.apache.geronimo.gshell.remote.transport.TransportServer;
import org.apache.geronimo.gshell.remote.transport.base.BaseTransportFactory;
import org.codehaus.plexus.component.annotations.Component;

/**
 * Produces TCP transport instances.
 *
 * @version $Rev$ $Date$
 */
@Component(role=TransportFactory.class, hint="tcp")
public class TcpTransportFactory
    extends BaseTransportFactory
{
    @Override
    protected Transport createTransport(final URI remote, final URI local) throws Exception {
        return new TcpTransport(remote, local);
    }

    @Override
    protected TransportServer createTransportServer(final URI location) throws Exception {
        return new TcpTransportServer(location);
    }

    static InetSocketAddress address(final URI location) throws UnknownHostException {
        InetSocketAddress addr = null;

        if (location != null) {
            addr = new InetSocketAddress(InetAddress.getByName(location.getHost()), location.getPort());
        }

        return addr;
    }
}