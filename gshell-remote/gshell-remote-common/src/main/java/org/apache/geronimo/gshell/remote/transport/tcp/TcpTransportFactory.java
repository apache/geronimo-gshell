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

import org.apache.geronimo.gshell.remote.transport.Transport;
import org.apache.geronimo.gshell.remote.transport.TransportFactory;
import org.apache.geronimo.gshell.remote.transport.TransportServer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Produces TCP transport instances.
 *
 * @version $Rev$ $Date$
 */
@Component(role=TransportFactory.class, hint="tcp")
public class TcpTransportFactory
    implements TransportFactory
{
    @Requirement
    private PlexusContainer container;

    //
    // NOTE: We use autowire() here to get a few components injected.  These are injected via setters.
    //
    
    public Transport connect(final URI remote, final URI local) throws Exception {
        assert remote != null;
        // local can be null

        TcpTransport transport = createTcpTransport(remote, local);

        container.autowire(transport);

        transport.connect();

        return transport;
    }

    protected TcpTransport createTcpTransport(final URI remote, final URI local) throws Exception {
        assert remote != null;
        // local can be null
        
        return new TcpTransport(remote, local);
    }

    public Transport connect(final URI remote) throws Exception {
        return connect(remote, null);
    }

    public TransportServer bind(final URI location) throws Exception {
        assert location != null;

        TcpTransportServer server = createTransportServer(location);

        container.autowire(server);

        server.bind();

        return server;
    }

    protected TcpTransportServer createTransportServer(final URI location) throws Exception {
        return new TcpTransportServer(location);
    }
}