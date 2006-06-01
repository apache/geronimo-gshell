/*
 * Copyright 2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.gshell.command;

import org.apache.xbean.finder.ResourceFinder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.Properties;
import java.util.HashMap;
import java.util.Set;
import java.util.Collections;

/**
 * Manager of command definitions and provides access to command instances.
 *
 * @version $Id$
 */
public class CommandManager
{
    private static final Log log = LogFactory.getLog(CommandManager.class);

    private Map<String,CommandDefinition> commandDefMap = new HashMap<String,CommandDefinition>();

    public CommandManager() throws CommandException {
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

    public void addCommandDefinition(final CommandDefinition def) {
        if (def == null) {
            throw new IllegalArgumentException("Def is null");
        }

        commandDefMap.put(def.getName(), def);

        if (log.isDebugEnabled()) {
            log.debug("Added definition: " + def);
        }
    }

    public CommandDefinition getCommandDefinition(String name) throws CommandNotFoundException {
        if (name == null) {
            throw new IllegalArgumentException("Name is null");
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
            throw new CommandNotFoundException(name);
        }

        return def;
    }

    public Command getCommand(final String name) throws CommandNotFoundException, CommandInstantiationException {
        // name checked by getCommandDefinition()

        CommandDefinition def = getCommandDefinition(name);

        //
        // TODO: This might change if we DI the class and let the container create
        //

        Command cmd;
        try {
            Class type = def.loadClass();
            cmd = (Command)type.newInstance();
        }
        catch (Exception e) {
            throw new CommandInstantiationException(name, e);
        }

        return cmd;
    }
    
    public Set<String> commandNames() {
        return Collections.unmodifiableSet(commandDefMap.keySet());
    }
}
