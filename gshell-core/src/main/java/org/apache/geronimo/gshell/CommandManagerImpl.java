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

package org.apache.geronimo.gshell;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandDefinition;
import org.apache.geronimo.gshell.command.CommandManager;
import org.apache.geronimo.gshell.command.CommandNotFoundException;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager of command definitions and provides access to command instances.
 *
 * @version $Rev$ $Date$
 */
@Component(role=CommandManager.class)
public class CommandManagerImpl
    implements CommandManager, Initializable
{
    private static final Logger log = LoggerFactory.getLogger(CommandManager.class);

    private Map<String, CommandDefinition> commandDefMap = new HashMap<String,CommandDefinition>();

    private Map<String,CommandDefinition> commandAliasMap = new HashMap<String,CommandDefinition>();

    @Requirement
    private PlexusContainer container;

    public void initialize() throws InitializationException {
        try {
            discoverCommands();
        }
        catch (Exception e) {
            throw new InitializationException("Failed to discover commands", e);
        }
    }

    private void discoverCommands() throws Exception {
        log.info("Discovering commands");

        List<ComponentDescriptor> descriptors = container.getComponentDescriptorList(Command.class.getName());
        for (ComponentDescriptor desc : descriptors) {
            //
            // HACK: Bridge old def from descriptor
            //

            Properties props = new Properties();

            props.put("name", desc.getRoleHint());
            props.put("class", desc.getImplementation());
            props.put("enable", true);
            props.put("category", "ignored");
            
            CommandDefinition def = new CommandDefinition(props);
            addCommandDefinition(def);
        }
    }

    public boolean addCommandDefinition(final CommandDefinition def) {
        assert def != null;

        boolean debug = log.isDebugEnabled();

        CommandDefinition prev = commandDefMap.put(def.getName(), def);
        if (debug) {
            log.debug("Added definition: " + def);
        }

        addCommandAliases(def);

        return prev != null;
    }

    private void addCommandAliases(final CommandDefinition def) {
        assert def != null;

        boolean debug = log.isDebugEnabled();

        for (String alias : def.getAliases()) {
            CommandDefinition prev = commandAliasMap.put(alias, def);
            if (debug) {
                log.debug("Added alias (to " + def.getName() + "): " + def);
                if (prev != null) {
                    log.debug("    Replaces previous alias to: " + prev.getName());
                }
            }
        }
    }

    public CommandDefinition getCommandDefinition(String name) throws CommandNotFoundException {
        assert name != null;
        assert name.trim().length() != 0;
        
        //
        // TODO: Issue warning if there is whitespace, that is a programming error (for someone)
        //       Investigate auto-trim and complain from the parser too, looks like we are catching
        //       non-traditional whitespace (ctrl chars, etc).
        //

        // Make sure there is not funky whitespace in there (from Telnet or something)
        name = name.trim();

        CommandDefinition def = commandDefMap.get(name);
        if (def == null) {
            def = commandAliasMap.get(name);

            if (def == null) {
                throw new CommandNotFoundException(name);
            }
        }

        return def;
    }

    public Set<String> commandNames() {
        return Collections.unmodifiableSet(commandDefMap.keySet());
    }

    public Collection<CommandDefinition> commandDefinitions() {
        return Collections.unmodifiableCollection(commandDefMap.values());
    }
}
