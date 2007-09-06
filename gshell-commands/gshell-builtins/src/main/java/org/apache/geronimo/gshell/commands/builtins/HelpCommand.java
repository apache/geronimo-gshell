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

import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.command.descriptor.CommandDescriptor;
import org.apache.geronimo.gshell.layout.LayoutManager;
import org.apache.geronimo.gshell.plugin.PluginCollector;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Display help
 *
 * @version $Rev$ $Date$
 */
@CommandComponent(id="help")
public class HelpCommand
    extends CommandSupport
{
    @Requirement
    private PluginCollector pluginCollector;

    @Requirement
    private LayoutManager layoutManager;

    protected Object doExecute() throws Exception {
        assert pluginCollector != null;
        assert layoutManager != null;

        io.out.println("Available commands:");

        Collection<CommandDescriptor> commands = pluginCollector.getCommandDescriptors();

        //
        // TODO: Need to ask the LayoutManager...
        //

        for (CommandDescriptor desc : commands) {
            io.out.print("  ");
            io.out.print(desc.getId());
            io.out.println();
        }
        
        io.out.println();

        return SUCCESS;
    }
}
