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

import org.apache.geronimo.gshell.ExitNotification;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.command.annotation.Requirement;
import org.apache.geronimo.gshell.io.PromptReader;
import org.apache.geronimo.gshell.remote.client.proxy.RemoteShellProxy;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Command to connect to a remote shell server.
 *
 * @version $Rev$ $Date$
 */
@CommandComponent(id="gshell-remote:rsh", description="Connect to a remote GShell server")
public class RshCommand
    extends CommandSupport
{
    @Option(name="-b", aliases={"--bind"}, metaVar="URI", description="Bind local address to URI")
    private URI local;

    @Option(name="-u", aliases={"--username"}, metaVar="USERNAME", description="Remote user name")
    private String username;

    @Option(name="-p", aliases={"--password"}, metaVar="PASSWORD", description="Remote user password")
    private String password;
    
    @Argument(metaVar="URI", required=true, index=0, description="Connect to remote server at URI")
    private URI remote;

    @Argument(metaVar="COMMAND", index=1, multiValued=true, description="Execute COMMAND in remote shell")
    private List<String> command = new ArrayList<String>();

    @Requirement
    private PromptReader prompter;

    @Requirement
    private RshClient client;

    protected Object doExecute() throws Exception {
        io.info("Connecting to: {}", remote);

        client.connect(remote, local);

        io.info("Connected");

        // If the username/password was not configured via cli, then prompt the user for the values
        if (username == null || password == null) {
            if (username == null) {
                username = prompter.readLine("Username: ");
            }

            if (password == null) {
                password = prompter.readPassword("Password: ");
            }

            //
            // TODO: Handle null inputs...
            //
        }

        client.login(username, password);

        // client.echo("HELLO");
        // Thread.sleep(1 * 1000);

        RemoteShellProxy shell = new RemoteShellProxy(client, io);

        Object rv = SUCCESS;

        try {
            shell.run(command.toArray());
        }
        catch (ExitNotification n) {
            // Make sure that we catch this notification, so that our parent shell doesn't exit when the remote shell does
            rv = n.code;
        }

        shell.close();
        
        io.verbose("Disconnecting");

        client.close();

        io.verbose("Disconnected");

        return rv;
    }
}