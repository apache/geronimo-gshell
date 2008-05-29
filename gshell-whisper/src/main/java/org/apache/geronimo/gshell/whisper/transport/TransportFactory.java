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

package org.apache.geronimo.gshell.whisper.transport;

import java.net.URI;

import org.apache.mina.common.IoHandler;

//
// FIXME: This won't compile under Java 6 due to the T. and S. bits (trying to select from a type variable)
//

/**
 * Factory for producing client and server transport implementations.
 *
 * @version $Rev$ $Date$
 */
public interface TransportFactory<T extends Transport, TC extends T.Configuration, S extends TransportServer, SC extends S.Configuration>
{
    String getScheme();
    
    T connect(URI remote, URI local, TC config) throws Exception;

    T connect(URI remote, URI local, IoHandler handler) throws Exception;

    /*
    Transport connect(URI remote, URI local) throws Exception;

    Transport connect(URI remote) throws Exception;
    */
    
    S bind(URI location, SC config) throws Exception;

    S bind(URI location, IoHandler handler) throws Exception;

    /*
    TransportServer bind(URI location) throws Exception;
    */
}