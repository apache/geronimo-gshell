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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Date;

import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.console.Console;
import org.apache.geronimo.gshell.console.JLineConsole;
import org.apache.geronimo.gshell.ExitNotification;
import org.apache.geronimo.gshell.ansi.Renderer;
import org.codehaus.plexus.component.annotations.Requirement;
import jline.Terminal;

/**
 * Command to connect to a remote shell server.
 *
 * @version $Rev$ $Date$
 */
@CommandComponent(id="rsh")
public class RshCommand
    extends CommandSupport
{
    @Argument(required=true)
    private URI location;

    @Requirement
    private Terminal terminal;

    @Requirement
    private RshClientFactory factory;

    private RshClient client;

    protected Object doExecute() throws Exception {
        io.out.println("Connecting to: " + location);

        client = factory.connect(location);

        io.out.println("Connected");

        client.handshake();

        Console.Executor executor = new Console.Executor() {
            public Result execute(final String line) throws Exception {
                assert line != null;

                client.echo(line);

                return Result.CONTINUE;
            }
        };

        JLineConsole console = new JLineConsole(executor, io, terminal);

        console.setPrompter(new Console.Prompter() {
            Renderer renderer = new Renderer();

            public String prompt() {
                String userName = "user";
                String hostName = "remote";
                String path = "/";

                return renderer.render("@|bold " + userName + "|@" + hostName + ":@|bold " + path + "|> ");
            }
        });

        console.setErrorHandler(new Console.ErrorHandler() {
            public Result handleError(final Throwable error) {
                assert error != null;

                log.error("Communication error: " + error, error);

                return Result.CONTINUE;
            }
        });

        console.run();
        
        client.close();

        return SUCCESS;
    }
}