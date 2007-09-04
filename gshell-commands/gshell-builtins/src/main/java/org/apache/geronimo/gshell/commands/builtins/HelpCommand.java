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

import java.util.List;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.IO;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.ComponentDescriptor;

/**
 * Display help
 *
 * @version $Rev$ $Date$
 */
@Component(role= Command.class, hint="help")
public class HelpCommand
    extends CommandSupport
{
    @Requirement
    private PlexusContainer container;

    protected Object doExecute() throws Exception {
        assert container != null;

        IO io = getIO();

        io.out.println("Available commands:");

        List<ComponentDescriptor> list = container.getComponentDescriptorList(Command.class.getName());

        for (ComponentDescriptor desc : list) {
            io.out.print("  ");
            io.out.print(desc.getRoleHint());
            io.out.println();
        }

        io.out.println();

        return SUCCESS;
    }
}
