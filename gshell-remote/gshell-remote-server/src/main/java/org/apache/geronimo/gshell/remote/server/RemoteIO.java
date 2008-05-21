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

package org.apache.geronimo.gshell.remote.server;

import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.whisper.stream.SessionInputStream;
import org.apache.geronimo.gshell.whisper.stream.SessionOutputStream;
import org.apache.geronimo.gshell.whisper.transport.Session;

/**
 * Container for <em>remote</em> input/output handles.
 * 
 * @version $Rev$ $Date$
 */
public class RemoteIO
    extends IO
{
    //
    // TODO: Figure out how to hook up STDERR to all of this muck
    //
    
    public RemoteIO(final Session session) {
        super(SessionInputStream.BINDER.lookup(session.getSession()), SessionOutputStream.BINDER.lookup(session.getSession()), false);
    }
}
