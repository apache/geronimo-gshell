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
import org.apache.geronimo.gshell.registry.CommandRegistration;
import org.apache.geronimo.gshell.registry.CommandRegistry;
import org.apache.geronimo.gshell.io.IO;
import org.codehaus.plexus.util.StringUtils;
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
    private CommandRegistry commandRegistry;

    @Argument
    private String commandName;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;

        if (commandName != null) {
            return displayCommandManual(context);
        }

        return displayAvailableCommands(context);
    }

    private Object displayCommandManual(final CommandContext context) {
        assert context != null;
        IO io = context.getIo();

        log.debug("Displaying help manual for command: {}", commandName);

        // FIXME: Should resolve the commandName/commandPath

        assert commandRegistry != null;
        Collection<CommandRegistration> registrations = commandRegistry.getRegistrations();

        for (CommandRegistration registration : registrations) {
            Command command = registration.getCommand();
            CommandDocumenter doc = command.getDocumenter();

            if (doc.getName().equals(commandName)) {
                doc.renderManual(io.out);

                return Result.SUCCESS;
            }
        }

        io.out.print("Command ");
        io.out.print(Renderer.encode(commandName, Code.BOLD));
        io.out.println(" not found.");

        io.out.print("Try ");
        io.out.print(Renderer.encode("help", Code.BOLD));
        io.out.println(" for a list of available commands.");

        io.out.println();

        return Result.FAILURE;
    }

    private Object displayAvailableCommands(final CommandContext context) {
        assert context != null;
        IO io = context.getIo();

        log.debug("Listing brief help for commands");

        // FIXME: Figure this out dynamically
        int maxNameLen = 20;

        assert commandRegistry != null;
        Collection<CommandRegistration> registrations = commandRegistry.getRegistrations();

        io.out.println("Available commands:");

        for (CommandRegistration registration : registrations) {
            Command command = registration.getCommand();
            CommandDocumenter doc = command.getDocumenter();

            String name = StringUtils.rightPad(doc.getName(), maxNameLen);
            String desc = doc.getDescription();

            io.out.print("  ");
            io.out.print(Renderer.encode(name, Code.BOLD));

            if (desc != null) {
                io.out.print("  ");
                io.out.println(desc);
            }
            else {
                io.out.println();
            }
        }

        io.out.println();

        return Result.SUCCESS;
    }
}
