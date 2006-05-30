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

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.HashMap;

/**
 * ???
 *
 * @version $Id$
 */
public class CommandManager
{
    private Map<String,Properties> commandDefMap = new HashMap<String,Properties>();

    public CommandManager() throws IOException {
        ResourceFinder resourceFinder = new ResourceFinder("META-INF/");

        Map<String, Properties> propertiesMap = resourceFinder.mapAllProperties("org.apache.geronimo.gshell.command");
        Iterator<String> iter = propertiesMap.keySet().iterator();

        while (iter.hasNext()) {
            String filename = iter.next();
            Properties props = propertiesMap.get(filename);

            String name = props.getProperty("name");
            if (name != null) {
                commandDefMap.put(name, props);
            }
        }
    }

    public Command getCommand(final String name) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        assert name != null;

        Properties props = commandDefMap.get(name);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class type = cl.loadClass(props.getProperty("class"));
        Command cmd = (Command)type.newInstance();

        return cmd;
    }
}
