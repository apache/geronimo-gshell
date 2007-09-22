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

import org.apache.geronimo.gshell.remote.transport.base.BaseTransportServer;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.transport.vmpipe.VmPipeAcceptor;

/**
 * Provides in-VM server-side support.
 *
 * @version $Rev$ $Date$
 */
public class VmTransportServer
    extends BaseTransportServer
{
    public VmTransportServer(final URI location) throws Exception {
        super(location, VmTransportFactory.address(location));
    }

    @Override
    protected IoAcceptor createAcceptor() throws Exception {
        return new VmPipeAcceptor();
    }
}