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

package org.apache.geronimo.gshell.command;

import org.apache.xbean.finder.ResourceFinder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.NullArgumentException;

import java.util.Map;
import java.util.Properties;
import java.util.HashMap;
import java.util.Set;
import java.util.Collections;
import java.util.Collection;

/**
 * Manager of command definitions and provides access to command instances.
 *
 * @version $Rev$ $Date$
 */
public class CommandManagerImpl
    implements CommandManager
{
    private static final Log log = LogFactory.getLog(CommandManager.class);

    private Map<String,CommandDefinition> commandDefMap = new HashMap<String,CommandDefinition>();

    private Map<String,CommandDefinition> commandAliasMap = new HashMap<String,CommandDefinition>();

    public CommandManagerImpl() throws CommandException {
        try {
            discoverCommands();
        }
        catch (Exception e) {
            throw new CommandException(e);
        }
    }

    private void discoverCommands() throws Exception {
        log.info("Discovering commands");

        ResourceFinder finder = new ResourceFinder("META-INF/");
        Map<String, Properties> map = finder.mapAllProperties("org.apache.geronimo.gshell.command");

        for (String filename : map.keySet()) {
            Properties props = map.get(filename);
            CommandDefinition def = new CommandDefinition(props);
            addCommandDefinition(def);
        }
    }

    public boolean addCommandDefinition(final CommandDefinition def) {
        if (def == null) {
            throw new IllegalArgumentException("Def is null");
        }

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
        if (name == null) {
            throw new NullArgumentException("name");
        }
        if (name.trim().length() == 0) {
            throw new IllegalArgumentException("Name is empty");
        }

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
