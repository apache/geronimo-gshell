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

package org.apache.geronimo.gshell.builtins;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.CommandManager;
import org.apache.geronimo.gshell.command.CommandDefinition;
import org.apache.geronimo.gshell.command.MessageSource;
import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.util.Arguments;

/**
 * Display help
 *
 * @version $Id$
 */
public class HelpCommand
    extends CommandSupport
{
    private CommandManager commandManager;

    public HelpCommand(final CommandManager commandManager) {
        super("help");

        assert commandManager != null;

        this.commandManager = commandManager;
    }

    /*

    TODO: Setter injection needs help...
    
    public void setCommandManager(final CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    */

    private CommandManager getCommandManager() {
        if (commandManager == null) {
            throw new IllegalStateException("Not initialized; missing command manger");
        }
        return commandManager;
    }

    protected int doExecute(final String[] args) throws Exception {
        assert args != null;

        MessageSource messages = getMessageSource();

        //
        // TODO: Optimize, move common code to CommandSupport
        //

        IO io = getIO();

        Options options = new Options();

        options.addOption(OptionBuilder.withLongOpt("help")
            .withDescription(messages.getMessage("cli.option.help"))
            .create('h'));

        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, args);

        boolean usage = false;
        String topic = null;

        String[] _args = line.getArgs();
        if (_args.length > 1) {
            io.err.println("Too many arguments: " + _args);
            return Command.FAILURE;
        }
        else if (_args.length == 1) {
            topic = _args[0];
        }
        else {
            usage = true;
        }

        if (usage || line.hasOption('h')) {
            io.out.print(getName());
            io.out.print(" -- ");
            io.out.println(messages.getMessage("cli.usage.description"));
            io.out.println();

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                io.out,
                80, // width (FIXME: Should pull from gshell.columns variable)
                getName() + " [options] [topic|command]",
                "",
                options,
                4, // left pad
                4, // desc pad
                "",
                false); // auto usage

            io.out.println();
            io.out.println(messages.getMessage("cli.usage.footer"));
            io.out.println();
            
            return Command.SUCCESS;
        }

        CommandManager manager = getCommandManager();

        //
        // TODO: Reuse our command bits...
        //

        if (topic.equals("topics")) {
            io.out.println("Available topics:");
            io.out.println("  topics");
            io.out.println("  commands");
            io.out.println();
        }
        else if (topic.equals("commands")) {
            io.out.println("Available commands (and aliases):");

            //
            // HACK: For now just list all know commands
            //

            for (CommandDefinition def : manager.commandDefinitions()) {
                io.out.print("  ");
                io.out.print(def.getName());

                // Include a list of aliases
                String[] aliases = def.getAliases();
                if (aliases.length != 0) {
                    io.out.print(" ( ");
                    io.out.print(Arguments.asString(aliases));
                    io.out.print(" )");
                }

                io.out.println();
            }

            io.out.println();
        }
        else {
            //
            // TODO: ...
            //

            io.err.println("Pending... sorry");
            io.err.println();
        }

        return Command.SUCCESS;
    }
}
