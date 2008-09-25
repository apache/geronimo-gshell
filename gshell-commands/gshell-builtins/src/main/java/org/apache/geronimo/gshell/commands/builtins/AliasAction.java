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

import org.apache.geronimo.gshell.alias.Alias;
import org.apache.geronimo.gshell.alias.AliasManager;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.i18n.MessageSource;
import org.apache.geronimo.gshell.io.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Autowired
    private AliasManager aliasManager;

    @Argument(index=0)
    private String name;

    @Argument(index=1)
    private String target;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();
        MessageSource messages = context.getCommand().getMessages();

        if (name == null) {
            log.debug("Listing defined aliases");

            Collection<Alias> aliases = aliasManager.getAliases();
            
            if (aliases.isEmpty()) {
                io.verbose("No aliases have been defined");
            }
            else {
                io.info("Defined aliases:");

                for (Alias alias : aliases) {
                    io.info("  {}='{}'", alias.getName(), alias.getTarget());
                }
            }
        }
        else if (target == null) {
            io.error("Missing argument: {}", messages.getMessage("command.argument.target.token"));
            return Result.FAILURE;
        }
        else {
            log.debug("Defining alias: {} -> {}", name, target);

            Alias alias = aliasManager.defineAlias(name, target);

            log.debug("Alias: {}", alias);
        }

        return Result.SUCCESS;
    }
}
