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

package org.apache.geronimo.gshell.remote.transport.ssl;

import java.net.URI;

import org.apache.geronimo.gshell.remote.transport.tcp.TcpTransport;
import org.apache.geronimo.gshell.remote.ssl.BogusSSLContextFactory;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.ssl.SSLFilter;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class SslTransport
    extends TcpTransport
{
    public SslTransport(final URI remote, final URI local) throws Exception {
        super(remote, local);
    }

    protected void init() throws Exception {
        super.init();

        DefaultIoFilterChainBuilder filterChain = connector.getFilterChain();

        //
        // TODO: Get the SSL context factory injected
        //
        
        SSLFilter sslFilter = new SSLFilter(BogusSSLContextFactory.getInstance(false));
        sslFilter.setUseClientMode(true);

        filterChain.addFirst("ssl", sslFilter);
    }
}