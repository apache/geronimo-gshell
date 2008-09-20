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

package org.apache.geronimo.gshell.whisper.transport.vm;

import java.net.URI;

import org.apache.geronimo.gshell.whisper.transport.TransportFactory;
import org.apache.geronimo.gshell.whisper.transport.TransportFactoryLocator;
import org.apache.geronimo.gshell.whisper.transport.TransportServer;
import org.apache.geronimo.gshell.whisper.SpringTestSupport;
import org.apache.mina.common.IoHandlerAdapter;

/**
 * Tests for the {@link VmTransportFactory} class.
 *
 * @version $Rev$ $Date$
 */
public class VmTransportFactoryTest
    extends SpringTestSupport
{
    private TransportFactory factory;

    private URI uri;

    private TransportFactoryLocator getLocator() {
        TransportFactoryLocator locator = (TransportFactoryLocator) applicationContext.getBean("transportFactoryLocator");
        assertNotNull(locator);

        return locator;
    }

    protected void onSetUp() throws Exception {
        uri = new URI("vm://local:1");

        factory = getLocator().locate(uri);

        assertNotNull(factory);
    }

    public void testBindWithConfig() throws Exception {
        VmTransportServer.Configuration config = new VmTransportServer.Configuration();
        config.setHandler(new IoHandlerAdapter());

        TransportServer server = factory.bind(uri, config);
        
        assertNotNull(server);
        
        assertTrue(server instanceof VmTransportServer);

        server.close();
    }

    public void testBindWithHandler() throws Exception {
        TransportServer server = factory.bind(uri, new IoHandlerAdapter());

        assertNotNull(server);

        assertTrue(server instanceof VmTransportServer);

        server.close();
    }
}