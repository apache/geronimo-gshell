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
import org.apache.geronimo.gshell.registry.AliasRegistry;
import org.apache.geronimo.gshell.registry.CommandResolver;
import org.apache.geronimo.gshell.registry.NoSuchAliasException;
import org.apache.geronimo.gshell.registry.NoSuchCommandException;
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

    @Autowired
    private AliasRegistry aliasRegistry;

    //
    // TODO: Rename to path or name
    //
    
    @Argument
    private String commandName;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;

        //
        // FIXME: Resolve commands first based on path, if there is one match, then display manual, else display brief listing
        //
        
        if (commandName != null) {
            return displayCommandManual(context);
        }

        return displayAvailableCommands(context);
    }

    private Object displayCommandManual(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        log.debug("Displaying help manual for command: {}", commandName);

        try {
            assert commandResolver != null;
            Command command = commandResolver.resolveCommand(commandName, context.getVariables());

            assert command != null;
            command.getDocumenter().renderManual(io.out);

            return Result.SUCCESS;
        }
        catch (NoSuchCommandException e) {
            try {
                assert aliasRegistry != null;
                String alias = aliasRegistry.getAlias(commandName);
                
                io.out.print("Command ");
                io.out.print(Renderer.encode(commandName, Code.BOLD));
                io.out.print(" is an alias to: ");
                io.out.println(Renderer.encode(alias, Code.BOLD));

                return Result.SUCCESS;
            }
            catch (NoSuchAliasException e1) {
                io.out.print("Command ");
                io.out.print(Renderer.encode(commandName, Code.BOLD));
                io.out.println(" not found.");

                io.out.print("Try ");
                io.out.print(Renderer.encode("help", Code.BOLD));
                io.out.println(" for a list of available commands.");

                return Result.FAILURE;
            }
        }
    }

    private Object displayAvailableCommands(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        log.debug("Listing brief help for commands");

        assert commandResolver != null;
        Collection<Command> commands = commandResolver.resolveCommands(null, context.getVariables());
        
        // Determine the maximun name length
        int maxNameLen = 0;
        for (Command command : commands) {
            int len = command.getDocumenter().getName().length();
            maxNameLen = Math.max(len, maxNameLen);
        }

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
