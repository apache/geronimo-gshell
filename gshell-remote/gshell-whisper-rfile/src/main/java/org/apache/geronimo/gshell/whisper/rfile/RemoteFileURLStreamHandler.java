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

package org.apache.geronimo.gshell.whisper.rfile;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * URL stream handler for the <tt>rfile</tt> (remote-file) protocol.
 *
 * @version $Rev$ $Date$
 */
public class RemoteFileURLStreamHandler
    extends URLStreamHandler
{
    public static final String PROTOCOL = "rfile";

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    //
    // TODO: We are going to need to get a handle on the current session to communicate with...
    //

    protected URLConnection openConnection(final URL url) throws IOException {
        assert url != null;

        String protocol = url.getProtocol();

        if (!PROTOCOL.equals(protocol)) {
            throw new IllegalArgumentException("Invalid protocol: " + protocol);
        }

        log.debug("Opening connection: {}", url);
        
        //
        // TODO: Need to request the remote file transfer, and get back the basic file details
        //       and then provide the connection below with the info and session to talk over
        //
        // OpenFileMessage msg = new OpenFileMessage();

        return new RemoteFileURLConnection(url /* TODO: Pass in state */);
    }
}