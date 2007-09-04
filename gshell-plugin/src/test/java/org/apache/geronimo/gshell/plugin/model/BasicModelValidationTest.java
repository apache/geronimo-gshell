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

package org.apache.geronimo.gshell.plugin.model;

import java.io.StringWriter;

import junit.framework.TestCase;
import org.apache.geronimo.gshell.plugin.model.io.xpp3.PluginXpp3Reader;
import org.apache.geronimo.gshell.plugin.model.io.xpp3.PluginXpp3Writer;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Basic validation of the plugin model muck.
 *
 * @version $Rev$ $Date$
 */
public class BasicModelValidationTest
    extends TestCase
{
    public void testRead1() throws Exception {
        PluginXpp3Reader reader = new PluginXpp3Reader();
        Plugin plugin = reader.read(getClass().getResourceAsStream("plugins1.xml"));

        System.out.println(plugin.getId());
        
        assertNotNull(plugin);
        assertEquals("testing", plugin.getName());

        assertNotNull(plugin.getCommands());
        assertEquals(1, plugin.getCommands().size());

        Command command = (Command) plugin.getCommands().get(0);
        assertNotNull(command);
        assertEquals("builtins.HelpCommand", command.getName());
        assertEquals("org.apache.geronimo.gshell.commands.builtins.HelpCommand", command.getImplementation());

        assertNotNull(command.getRequirements());
        assertEquals(1, command.getRequirements().size());

        Requirement req = (Requirement) command.getRequirements().get(0);
        assertNotNull(req);
        assertEquals("org.codehaus.plexus.PlexusContainer", req.getRole());
        assertEquals("container", req.getField());

        assertNotNull(command.getConfiguration());

        assertEquals(Xpp3Dom.class, command.getConfiguration().getClass());

        //
        // TODO: Validate the configuration bits...
        //
    }

    public void testDump1() throws Exception {
        Plugin plugin = new Plugin();
        plugin.setId("foo");

        PluginXpp3Writer writer = new PluginXpp3Writer();
        StringWriter out = new StringWriter();
        writer.write(out, plugin);
        
        System.out.println(out.getBuffer());
    }
}
