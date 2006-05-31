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

package org.apache.geronimo.gshell.commands.standard;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.CommandManager;
import org.apache.geronimo.gshell.console.IO;

/**
 * Display command help
 *
 * @version $Id$
 */
public class HelpCommand
    extends CommandSupport
{
    public HelpCommand() {
        super("help");
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

        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, args);

        if (line.hasOption('h')) {
            io.out.println(getName() + " -- display gshell help");
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

        //
        // HACK: Need to DI this guy, but for now this will work
        //
        
        CommandManager manager = new CommandManager();

        //
        // HACK: For now just list all know commands
        //

        for (String name : manager.commandNames()) {
            io.out.println(name);
        }

        return Command.SUCCESS;
    }
}
