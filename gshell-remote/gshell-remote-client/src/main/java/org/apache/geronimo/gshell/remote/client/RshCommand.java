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

package org.apache.geronimo.gshell.remote.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import jline.Terminal;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Command to connect to a remote shell server.
 *
 * @version $Rev$ $Date$
 */
@CommandComponent(id="rsh")
public class RshCommand
    extends CommandSupport
{
    //
    // TODO: Add support to bind to a local address, also look at man pages for rsh and ssh for more options which might want to support.
    //
    
    @Argument(metaVar="LOCATION", required=true, index=0)
    private URI location;

    @Argument(metaVar="COMMAND", index=1)
    private List<String> command = new ArrayList<String>();

    @Requirement
    private Terminal terminal;

    @Requirement
    private RshClientFactory factory;

    private RshClient client;

    protected Object doExecute() throws Exception {
        io.info("Connecting to: {}", location);

        client = factory.connect(location);

        io.info("Connected");

        client.login("jason", "password");

        RemoteShellProxy shell = new RemoteShellProxy(client, io, terminal);

        shell.run(command.toArray());

        shell.close();
        
        io.verbose("Disconnecting");

        client.close();

        io.verbose("Disconnected");
        
        return SUCCESS;
    }
}