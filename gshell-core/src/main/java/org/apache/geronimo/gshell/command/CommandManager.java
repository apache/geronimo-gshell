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

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Collections;

/**
 * ???
 *
 * @version $Id$
 */
public class CommandManager
{
    private Map<String,Properties> commandDefMap = new HashMap<String,Properties>();

    public CommandManager() throws CommandException {
        try {
            discoverCommands();
        }
        catch (Exception e) {
            throw new CommandException(e);
        }
    }

    private void discoverCommands() throws Exception {
        ResourceFinder finder = new ResourceFinder("META-INF/");
        Map<String, Properties> map = finder.mapAllProperties("org.apache.geronimo.gshell.command");
        Iterator<String> iter = map.keySet().iterator();

        for (String filename : map.keySet()) {
            Properties props = map.get(filename);

            String name = props.getProperty("name");
            if (name != null) {
                commandDefMap.put(name, props);
            }
        }
    }

    public Command getCommand(final String name) throws CommandNotFoundException, CommandInstantiationException {
        if (name == null) {
            throw new IllegalArgumentException("Name is null");
        }
        if (name.trim().length() == 0) {
            throw new IllegalArgumentException("Name is empty");
        }

        Properties props = commandDefMap.get(name);

        // No props means command was not discovered
        if (props == null) {
            throw new CommandNotFoundException(name);
        }

        String classname = props.getProperty("class");
        if (classname == null) {
            throw new CommandInstantiationException("Missing 'class' property for command: " + name);
        }

        Command cmd;
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class type = cl.loadClass(classname);
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
