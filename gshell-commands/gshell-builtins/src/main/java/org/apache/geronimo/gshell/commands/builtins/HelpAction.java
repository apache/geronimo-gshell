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

package org.apache.geronimo.gshell.commands.builtins;

import org.apache.geronimo.gshell.ansi.Code;
import org.apache.geronimo.gshell.ansi.Renderer;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.CommandDocumenter;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.registry.CommandResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

/**
 * Display command help.
 *
 * @version $Rev$ $Date$
 */
public class HelpAction
    implements CommandAction
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private CommandResolver commandResolver;

    @Argument
    private String commandName;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        Collection<Command> commands = commandResolver.resolveCommands(commandName, context.getVariables());

        if (commands.isEmpty()) {
            io.out.print("Command ");
            io.out.print(Renderer.encode(commandName, Code.BOLD));
            io.out.println(" not found.");

            io.out.print("Try ");
            io.out.print(Renderer.encode("help", Code.BOLD));
            io.out.println(" for a list of available commands.");

            return Result.FAILURE;
        }
        else if (commands.size() == 1) {
            Command command = commands.iterator().next();
            command.getDocumenter().renderManual(io.out);
            
            return Result.SUCCESS;
        }
        else {
            return displayAvailableCommands(context, commands);
        }
    }

    private Object displayAvailableCommands(final CommandContext context, final Collection<Command> commands) throws Exception {
        assert context != null;
        assert commands != null;

        log.debug("Listing brief help for commands");

        // Determine the maximun name length
        int maxNameLen = 0;
        for (Command command : commands) {
            int len = command.getDocumenter().getName().length();
            maxNameLen = Math.max(len, maxNameLen);
        }

        IO io = context.getIo();
        io.out.println("Available commands:");
        for (Command command : commands) {
            CommandDocumenter documenter = command.getDocumenter();

            String formattedName = String.format("%-" + maxNameLen + "s", documenter.getName());
            String desc = documenter.getDescription();

            io.out.print("  ");
            io.out.print(Renderer.encode(formattedName, Code.BOLD));

            if (desc != null) {
                io.out.print("  ");
                io.out.println(desc);
            }
            else {
                io.out.println();
            }
        }

        return Result.SUCCESS;
    }
}
