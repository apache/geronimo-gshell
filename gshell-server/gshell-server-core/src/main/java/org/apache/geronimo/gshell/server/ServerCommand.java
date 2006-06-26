/*
 * Copyright 2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.gshell.server;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.MessageSource;
import org.apache.geronimo.gshell.command.CommandException;
import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.server.SocketServerDaemon.SocketHandler;

import java.net.Socket;

/**
 * Starts a Shell server.
 *
 * @version $Id$
 */
public class ServerCommand
    extends CommandSupport
{
    private int port = 5057;

    public ServerCommand() {
        super("server");
    }

    protected Options getOptions() {
        MessageSource messages = getMessageSource();

        Options options = super.getOptions();

        options.addOption(OptionBuilder.withLongOpt("port")
            .withDescription(messages.getMessage("cli.option.port"))
            .hasArg()
            .create('p'));

        return options;
    }

    protected boolean processCommandLine(final CommandLine line) throws CommandException {
        assert line != null;

        if (line.hasOption('p')) {
            String tmp = line.getOptionValue('p');
            port = Integer.parseInt(tmp);
        }

        return false;
    }

    protected Object doExecute(final Object[] args) throws Exception {
        assert args != null;

        SocketHandler handler = new SocketHandler() {
            ShellServer server = new ShellServer();

            public void handle(final Socket socket) throws Exception {
                assert socket != null;

                server.service(socket);
            }
        };

        SocketServerDaemon daemon = new SocketServerDaemon(port, handler);

        IO io = getIO();
        MessageSource messages = getMessageSource();

        io.out.println(messages.getMessage("info.listening", port));
        io.flush();

        daemon.start();

        return Command.SUCCESS;
    }
}
