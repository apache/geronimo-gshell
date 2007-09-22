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

package org.apache.geronimo.gshell.remote.transport.base;

import java.net.URI;

import org.apache.geronimo.gshell.remote.transport.Transport;
import org.apache.geronimo.gshell.remote.transport.TransportFactory;
import org.apache.geronimo.gshell.remote.transport.TransportServer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Support for {@link TransportFactory} implementations.
 *
 * @version $Rev$ $Date$
 */
public abstract class BaseTransportFactory
    implements TransportFactory
{
    @Requirement
    protected PlexusContainer container;

    //
    // NOTE: We use autowire() here to get a few components injected.  These are injected via setters.
    //

    public Transport connect(final URI remote, final URI local) throws Exception {
        assert remote != null;
        // local can be null

        Transport transport = createTransport(remote, local);

        container.autowire(transport);

        transport.connect();

        return transport;
    }

    protected abstract Transport createTransport(final URI remote, final URI local) throws Exception;

    public Transport connect(final URI remote) throws Exception {
        return connect(remote, null);
    }

    public TransportServer bind(final URI location) throws Exception {
        assert location != null;

        TransportServer server = createTransportServer(location);

        container.autowire(server);

        server.bind();

        return server;
    }

    protected abstract TransportServer createTransportServer(final URI location) throws Exception;
}