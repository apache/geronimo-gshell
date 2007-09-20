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

import org.apache.geronimo.gshell.remote.transport.tcp.TcpTransportServer;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.transport.vmpipe.VmPipeAcceptor;
import org.apache.mina.transport.vmpipe.VmPipeAddress;

/**
 * Provides in-VM server-side support.
 *
 * @version $Rev$ $Date$
 */
public class VmTransportServer
    extends TcpTransportServer
{
    public VmTransportServer(final URI location) throws Exception {
        super(location, new VmPipeAddress(location.getPort()));
    }

    protected IoAcceptor createAcceptor() throws Exception {
        return new VmPipeAcceptor();
    }
}