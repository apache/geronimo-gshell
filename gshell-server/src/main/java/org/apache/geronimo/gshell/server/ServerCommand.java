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
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.console.IO;

/**
 * Starts a GShell server.
 *
 * @version $Id$
 */
public class ServerCommand
    extends CommandSupport
{
    private int port = GShellDaemon.DEFAULT_PORT;

    private boolean background = false;

    public ServerCommand() {
        super("server");
    }

    protected int doExecute(final String[] args) throws Exception {
        assert args != null;

        //
        // TODO: Optimize, move common code to CommandSupport
        //

        IO io = getIO();

        Options options = new Options();

        options.addOption(OptionBuilder.withLongOpt("help")
            .withDescription("Display this help message")
            .create('h'));

        options.addOption(OptionBuilder.withLongOpt("port")
            .withDescription("Use a specified port number")
            .hasArg()
            .create('p'));

        options.addOption(OptionBuilder.withLongOpt("background")
            .withDescription("Run as a daemon in the background")
            .create('b'));

        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, args);

        if (line.hasOption('h')) {
            io.out.println(getName() + " -- starts a GShell server");
            io.out.println();

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                io.out,
                80, // width (FIXME: Should pull from gshell.columns variable)
                getName() + " [options]",
                "",
                options,
                4, // left pad
                4, // desc pad
                "",
                false); // auto usage

            io.out.println();

            return Command.SUCCESS;
        }

        if (line.hasOption('p')) {
            String tmp = line.getOptionValue('p');
            port = Integer.parseInt(tmp);
        }

        if (line.hasOption('b')) {
            background = true;
        }

        server();

        return Command.SUCCESS;
    }

    private void server() throws Exception {
        GShellDaemon daemon = new GShellDaemon(port, background);

        //
        // NOTE: Spit this out before hand, since if not --background
        //       start() will not return right away
        //

        IO io = getIO();
        io.out.println("Listening for connections on port: " + port);
        io.flush();

        daemon.start();
    }
}
