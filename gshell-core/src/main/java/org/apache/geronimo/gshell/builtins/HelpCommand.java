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

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.CommandManager;
import org.apache.geronimo.gshell.command.CommandDefinition;
import org.apache.geronimo.gshell.command.MessageSource;
import org.apache.geronimo.gshell.command.CommandException;
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

    private String topic;

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

    protected String getUsage() {
        return super.getUsage() + " [topic|command]";
    }

    protected boolean processCommandLine(final CommandLine line) throws CommandException {
        assert line != null;

        String[] args = line.getArgs();

        IO io = getIO();
        MessageSource messages = getMessageSource();

        if (args.length == 0 || args.length > 1) {
            io.err.println(messages.getMessage("info.unexpected_args", Arguments.asString(args)));
            return true;
        }
        else if (args.length == 1) {
            topic = args[0];
        }

        return false;
    }

    protected Object doExecute(final Object[] args) throws Exception {
        assert args != null;

        IO io = getIO();
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
