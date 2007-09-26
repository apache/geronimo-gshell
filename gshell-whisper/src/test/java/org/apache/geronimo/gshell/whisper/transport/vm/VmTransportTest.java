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

import org.apache.geronimo.gshell.whisper.transport.InvalidLocationException;
import org.apache.geronimo.gshell.whisper.transport.TransportFactory;
import org.apache.geronimo.gshell.whisper.transport.TransportFactoryLocator;
import org.apache.geronimo.gshell.whisper.transport.ssl.SslTransportFactory;
import org.apache.geronimo.gshell.whisper.transport.tcp.TcpTransportFactory;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * Tests for the {@link VmTransport} class.
 *
 * @version $Rev$ $Date$
 */
public class VmTransportTest
    extends PlexusTestCase
{
    TransportFactoryLocator locator;

    protected void setUp() throws Exception {
        super.setUp();

        locator = (TransportFactoryLocator) lookup(TransportFactoryLocator.class);

        assertNotNull(locator);
    }

    public void testLocate_vm() throws Exception {
        URI uri = new URI("vm://local:1");

        TransportFactory factory = locator.locate(uri);

        assertTrue(factory instanceof VmTransportFactory);

        assertNotNull(factory);
    }

    public void testLocate_tcp() throws Exception {
        URI uri = new URI("tcp://localhost:9999");

        TransportFactory factory = locator.locate(uri);

        assertTrue(factory instanceof TcpTransportFactory);

        assertNotNull(factory);
    }

    public void testLocate_ssl() throws Exception {
        URI uri = new URI("ssl://localhost:9999");

        TransportFactory factory = locator.locate(uri);

        assertTrue(factory instanceof SslTransportFactory);

        assertNotNull(factory);
    }

    public void testLocate_unknown() throws Exception {
        URI uri = new URI("unknown://localhost:9999");

        try {
            locator.locate(uri);
            fail();
        }
        catch (ComponentLookupException expected) {}
    }

    public void testLocate_badLocation() throws Exception {
        URI uri = new URI("localhost");

        try {
            locator.locate(uri);
            fail();
        }
        catch (InvalidLocationException expected) {}
    }
}