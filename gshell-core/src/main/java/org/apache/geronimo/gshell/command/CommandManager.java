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
import java.util.Properties;
import java.util.Iterator;
import java.util.Map;

/**
 * ???
 *
 * @version $Id: Command.java 407908 2006-05-19 13:47:49 -0700 (Fri, 19 May 2006) jdillon $
 */
public class CommandManager
{
    public CommandManager() throws IOException {
        ResourceFinder resourceFinder = new ResourceFinder("META-INF/");

        Map<String, Properties> propertiesMap = resourceFinder.mapAllProperties("org.apache.geronimo.gshell.command");
        Iterator<String> iter = propertiesMap.keySet().iterator();

        while (iter.hasNext()) {
            String name = iter.next();
            Properties props = propertiesMap.get(name);
            System.err.println("name: " + name);
            System.err.println(props);
            System.err.println("----");
        }
    }

    //
    // HACK: Testing...
    //

    public static void main(final String[] args) throws Exception {
        new CommandManager();
    }
}
