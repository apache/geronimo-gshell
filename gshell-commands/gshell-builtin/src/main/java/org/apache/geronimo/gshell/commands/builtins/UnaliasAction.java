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

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.registry.AliasRegistry;
import org.apache.geronimo.gshell.registry.NoSuchAliasException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Undefine an alias.
 *
 * @version $Rev$ $Date$
 */
public class UnaliasAction
    implements CommandAction
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AliasRegistry aliasRegistry;
    
    @Argument(index=0, required=true)
    private String name;

    public UnaliasAction(final AliasRegistry aliasRegistry) {
        assert aliasRegistry != null;
        this.aliasRegistry = aliasRegistry;
    }

    public Object execute(final CommandContext context) {
        assert context != null;
        IO io = context.getIo();

        log.debug("Undefining alias: {}", name);

        try {
            aliasRegistry.removeAlias(name);

            return Result.SUCCESS;
        }
        catch (NoSuchAliasException e) {
            io.error("No alias defined: {}", name);

            return Result.FAILURE;
        }
    }
}