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

package org.apache.geronimo.gshell.remote.transport.vm;

import java.net.URI;

import org.apache.geronimo.gshell.remote.transport.Transport;
import org.apache.geronimo.gshell.remote.transport.TransportFactory;
import org.apache.geronimo.gshell.remote.transport.TransportServer;
import org.apache.geronimo.gshell.remote.transport.base.BaseTransportFactory;
import org.apache.mina.transport.vmpipe.VmPipeAddress;
import org.codehaus.plexus.component.annotations.Component;

/**
 * Produces in-VM transport instances.
 *
 * @version $Rev$ $Date$
 */
@Component(role=TransportFactory.class, hint="vm")
public class VmTransportFactory
    extends BaseTransportFactory
{
    @Override
    protected Transport createTransport(final URI remote, final URI local) throws Exception {
        return new VmTransport(remote, local);
    }

    @Override
    protected TransportServer createTransportServer(final URI location) throws Exception {
        return new VmTransportServer(location);
    }

    static VmPipeAddress address(final URI location) {
        VmPipeAddress addr = null;

        if (location != null) {
            addr = new VmPipeAddress(location.getPort());
        }

        return addr;
    }
}