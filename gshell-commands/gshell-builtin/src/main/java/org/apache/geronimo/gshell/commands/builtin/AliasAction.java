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

package org.apache.geronimo.gshell.commands.builtin;

import org.apache.geronimo.gshell.ansi.AnsiCode;
import org.apache.geronimo.gshell.ansi.AnsiRenderer;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.i18n.MessageSource;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.registry.AliasRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Define an alias.
 *
 * @version $Rev$ $Date$
 */
public class AliasAction
    implements CommandAction
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AliasRegistry aliasRegistry;

    @Argument(index=0)
    private String name;

    @Argument(index=1)
    private String target;

    public AliasAction(final AliasRegistry aliasRegistry) {
        assert aliasRegistry != null;
        this.aliasRegistry = aliasRegistry;
    }

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;

        if (name == null) {
            return listAliases(context);
        }
        else {
            return defineAlias(context);
        }
    }

    private Object listAliases(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        log.debug("Listing defined aliases");

        Collection<String> names = aliasRegistry.getAliasNames();

        if (names.isEmpty()) {
            io.info("No aliases have been defined");
        }
        else {
            // Determine the maximun name length
            int maxNameLen = 0;
            for (String name : names) {
                if (name.length() > maxNameLen) {
                    maxNameLen = name.length();
                }
            }

            io.out.println("Defined aliases:");
            for (String name : names) {
                String alias = aliasRegistry.getAlias(name);
                String formattedName = String.format("%-" + maxNameLen + "s", name);

                io.out.print("  ");
                io.out.print(AnsiRenderer.encode(formattedName, AnsiCode.BOLD));

                io.out.print("  ");
                io.out.print("Alias to: ");
                io.out.println(alias);
            }
        }

        return Result.SUCCESS;
    }

    private Object defineAlias(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        if (target == null) {
            MessageSource messages = context.getCommand().getMessages();
            io.error("Missing argument: {}", messages.getMessage("command.argument.target.token"));

            return Result.FAILURE;
        }

        log.debug("Defining alias: {} -> {}", name, target);

        aliasRegistry.registerAlias(name, target);

        return Result.SUCCESS;
    }
}
