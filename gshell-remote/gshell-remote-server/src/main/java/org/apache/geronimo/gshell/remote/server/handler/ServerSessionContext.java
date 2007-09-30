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

package org.apache.geronimo.gshell.remote.server.handler;

import java.security.PublicKey;
import java.util.UUID;

import org.apache.geronimo.gshell.common.tostring.ToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.remote.RemoteShell;
import org.apache.geronimo.gshell.remote.server.RemoteIO;
import org.apache.geronimo.gshell.remote.server.RemoteShellContainer;
import org.apache.geronimo.gshell.shell.Environment;
import org.apache.geronimo.gshell.whisper.session.SessionAttributeBinder;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class ServerSessionContext
{
    public static final SessionAttributeBinder<ServerSessionContext> BINDER = new SessionAttributeBinder<ServerSessionContext>(ServerSessionContext.class);

    /** The session id. */
    public final UUID id = UUID.randomUUID();

    /** The remote client's public key. */
    public PublicKey pk;

    /** The remote client's logged in username. */
    public String username;

    /** The container which the remote shell is running in. */
    public RemoteShellContainer container;

    /** The I/O context for the remote shell. */
    public RemoteIO io;

    /** The environment for the remote shell. */
    public Environment env;

    /** The remote shell instance. */
    public RemoteShell shell;

    public int hashCode() {
        return id.hashCode();
    }

    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .toString();
    }
}
