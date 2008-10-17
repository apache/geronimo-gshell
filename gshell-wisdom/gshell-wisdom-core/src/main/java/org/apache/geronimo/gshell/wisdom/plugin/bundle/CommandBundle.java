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

package org.apache.geronimo.gshell.wisdom.plugin.bundle;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.registry.AliasRegistry;
import org.apache.geronimo.gshell.registry.CommandRegistry;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.List;

/**
 * A bundle of {@link Command} instances.
 *
 * @version $Rev$ $Date$
 */
public class CommandBundle
    extends BundleSupport
{
    @Autowired
    private CommandRegistry commandRegistry;

    @Autowired
    private AliasRegistry aliasRegistry;

    private List<Command> commands;

    private Map<String,String> aliases;

    public CommandBundle(final String name) {
        super(name);
    }

    public List<Command> getCommands() {
        return commands;
    }

    public void setCommands(final List<Command> commands) {
        assert commands != null;
        
        this.commands = commands;
    }

    public Map<String, String> getAliases() {
        return aliases;
    }

    public void setAliases(final Map<String, String> aliases) {
        assert aliases != null;

        this.aliases = aliases;
    }

    protected void doEnable() throws Exception {
        assert commandRegistry != null;
        for (Command command : commands) {
            commandRegistry.registerCommand(command);
        }

        assert aliasRegistry != null;
        for (String name : aliases.keySet()) {
            aliasRegistry.registerAlias(name, aliases.get(name));
        }
    }

    protected void doDisable() throws Exception {
        assert commandRegistry != null;
        for (Command command : commands) {
            commandRegistry.removeCommand(command);
        }

        assert aliasRegistry != null;
        for (String name : aliases.keySet()) {
            aliasRegistry.removeAlias(name);
        }
    }
}