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

import org.apache.geronimo.gshell.command.CommandDefinition;
import org.apache.geronimo.gshell.command.CommandManager;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.IO;
import org.apache.geronimo.gshell.common.Arguments;

/**
 * Display help
 *
 * @version $Rev$ $Date$
 */
public class HelpCommand
    extends CommandSupport
{
    // @Requirement
    private CommandManager commandManager;

    public HelpCommand() {
        super("help");
    }

    protected Object doExecute() throws Exception {
        IO io = getIO();

        io.out.println("Available commands:");

        for (CommandDefinition def : commandManager.commandDefinitions()) {
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

        return SUCCESS;
    }
}
