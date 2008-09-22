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

import java.net.URI;

import org.apache.geronimo.gshell.whisper.transport.Transport;
import org.apache.geronimo.gshell.whisper.transport.TransportFactory;
import org.apache.geronimo.gshell.whisper.transport.TransportServer;
import org.apache.geronimo.gshell.spring.BeanContainerAware;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.mina.common.IoHandler;

/**
 * Support for {@link TransportFactory} implementations.
 *
 * @version $Rev$ $Date$
 */
public abstract class BaseTransportFactory<T extends BaseTransport, TC extends Transport.Configuration, S extends BaseTransportServer, SC extends TransportServer.Configuration>
    implements TransportFactory, BeanContainerAware
{
    private final String scheme;

    private BeanContainer container;

    protected BaseTransportFactory(final String scheme) {
        assert scheme != null;
        
        this.scheme = scheme;
    }

    public String getScheme() {
        return scheme;
    }

    public void setBeanContainer(final BeanContainer container) {
        assert container != null;

        this.container = container;
    }

    //
    // Transport (Client) Connection
    //

    @SuppressWarnings({"unchecked"})
    protected T createTransport() {
        return (T)container.getBean(scheme + "Transport", Transport.class);
    }

    public T connect(final URI remote, final URI local, final TC config) throws Exception {
        assert remote != null;
        assert config != null;
        // local can be null

        T transport = createTransport();
        transport.setConfiguration(config);
        transport.connect(remote, local);

        return transport;
    }

    public T connect(final URI remote, final URI local, final IoHandler handler) throws Exception {
        assert remote != null;
        assert handler != null;
        // local can be null

        T transport = createTransport();
        transport.getConfiguration().setHandler(handler);
        transport.connect(remote, local);

        return transport;
    }

    //
    // TransportServer Binding
    //

    @SuppressWarnings({"unchecked"})
    protected S createTransportServer() {
        return (S)container.getBean(scheme + "TransportServer", TransportServer.class);
    }

    public S bind(final URI location, final SC config) throws Exception {
        assert location != null;
        assert config != null;

        S server = createTransportServer();
        server.setConfiguration(config);
        server.bind(location);

        return server;
    }

    public S bind(final URI location, final IoHandler handler) throws Exception {
        assert location != null;
        assert handler != null;

        S server = createTransportServer();
        server.getConfiguration().setHandler(handler);
        server.bind(location);

        return server;
    }
}