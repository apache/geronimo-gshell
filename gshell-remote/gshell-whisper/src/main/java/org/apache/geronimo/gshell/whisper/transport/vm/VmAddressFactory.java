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

import org.apache.geronimo.gshell.whisper.transport.base.AddressFactory;
import org.apache.mina.transport.vmpipe.VmPipeAddress;
import org.codehaus.plexus.component.annotations.Component;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
@Component(role=AddressFactory.class, hint="vm")
public class VmAddressFactory
    implements AddressFactory<VmPipeAddress>
{
    public VmPipeAddress create(URI location) throws Exception {
        VmPipeAddress addr = null;

        if (location != null) {
            addr = new VmPipeAddress(location.getPort());
        }

        return addr;
    }
}