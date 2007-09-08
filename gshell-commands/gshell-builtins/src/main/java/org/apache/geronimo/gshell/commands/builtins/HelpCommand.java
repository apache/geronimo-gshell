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

import java.util.Collection;

import org.apache.geronimo.gshell.ansi.Code;
import org.apache.geronimo.gshell.ansi.Renderer;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.command.descriptor.CommandDescriptor;
import org.apache.geronimo.gshell.layout.LayoutManager;
import org.apache.geronimo.gshell.plugin.PluginCollector;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;

/**
 * Display help
 *
 * @version $Rev$ $Date$
 */
@CommandComponent(id="help", description="Show command help")
public class HelpCommand
    extends CommandSupport
{
    @Requirement
    private PluginCollector pluginCollector;

    @Requirement
    private LayoutManager layoutManager;

    private Renderer renderer = new Renderer();

    protected Object doExecute() throws Exception {
        assert pluginCollector != null;
        assert layoutManager != null;

        //
        // TODO: Get this from branding theme thingy
        //

        io.out.println();
        io.out.println("For information about Apache Geronimo, visit:");
        io.out.println("    http://geronimo.apache.org ");
        io.out.println();

        io.out.println("Available commands:");

        Collection<CommandDescriptor> commands = pluginCollector.getCommandDescriptors();

        // Figure out the maximum length of a command name
        int maxNameLen = 0;
        for (CommandDescriptor desc : commands) {
            if (desc.getId().length() > maxNameLen) {
                maxNameLen = desc.getId().length();
            }
        }

        //
        // TODO: Need to ask the LayoutManager...
        //

        for (CommandDescriptor d : commands) {
            // Hide commands if they don't have descriptions
            String name = d.getId();
            name = StringUtils.rightPad(name, maxNameLen);

            io.out.print("  ");
            io.out.print(renderer.render(Renderer.encode(name, Code.BOLD)));

            String desc = d.getDescription();

            if (desc != null) {
                io.out.print("  ");
                io.out.println(desc);
            }
            else {
                io.out.println();
            }
        }

        /*
        io.out.println();
        io.out.println("For help on a specific command type:");
        io.out.println("    help <command>");
        */

        io.out.println();

        return SUCCESS;
    }
}
