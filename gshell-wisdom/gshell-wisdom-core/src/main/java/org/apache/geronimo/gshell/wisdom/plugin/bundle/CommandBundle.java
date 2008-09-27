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
import org.apache.geronimo.gshell.registry.CommandRegistry;
import org.apache.geronimo.gshell.registry.AliasRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * A bundle of {@link Command} instances.
 *
 * @version $Rev$ $Date$
 */
public class CommandBundle
    implements Bundle
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private CommandRegistry commandRegistry;

    @Autowired
    private AliasRegistry aliasRegistry;

    private boolean enabled = false;

    private String name;

    private Map<String,Command> commands;

    private Map<String,String> aliases;

    public CommandBundle(final String name) {
        assert name != null;

        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Map<String, Command> getCommands() {
        return commands;
    }

    public void setCommands(final Map<String, Command> commands) {
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

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() throws Exception {
        if (enabled) {
            throw new IllegalStateException("Already enabled");
        }

        log.debug("Enabling");

        for (String name : commands.keySet()) {
            commandRegistry.registerCommand(name, commands.get(name));
        }

        for (String name : aliases.keySet()) {
            aliasRegistry.registerAlias(name, aliases.get(name));
        }

        enabled = true;
    }

    public void disable() throws Exception {
        if (!enabled) {
            throw new IllegalStateException("Not enabled");
        }

        log.debug("Disabling");
        
        for (String name : commands.keySet()) {
            commandRegistry.removeCommand(name);
        }

        for (String name : aliases.keySet()) {
            aliasRegistry.removeAlias(name);
        }

        enabled = false;
    }
}