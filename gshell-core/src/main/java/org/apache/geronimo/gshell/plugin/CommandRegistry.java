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

package org.apache.geronimo.gshell.plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.gshell.command.descriptor.CommandDescriptor;
import org.apache.geronimo.gshell.command.descriptor.CommandSetDescriptor;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryEvent;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryListener;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers command components as they are discovered by the container.
 * 
 * @version $Rev$ $Date$
 */
@Component(role= CommandRegistry.class)
public class CommandRegistry
    implements ComponentDiscoveryListener
{
    public static final String ID = "gshell-command-regsitry";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Map<String,CommandDescriptor> commandDescriptors = new HashMap<String,CommandDescriptor>();

    public String getId() {
        return ID;
    }
    
    public void componentDiscovered(final ComponentDiscoveryEvent event) {
        assert event != null;

        ComponentSetDescriptor setDescriptor = event.getComponentSetDescriptor();

        if (setDescriptor instanceof CommandSetDescriptor) {
            CommandSetDescriptor commands = (CommandSetDescriptor) setDescriptor;

            for (CommandDescriptor desc : commands.getCommandDescriptors()) {
                String id = desc.getId();

                if (commandDescriptors.containsKey(id)) {
                    log.error("Ignoring duplicate command id: {}", id);
                }
                else {
                    log.debug("Found command: {}", id);
                    commandDescriptors.put(id, desc);
                }
            }
        }
    }

    public Collection<CommandDescriptor> getCommandDescriptors() {
        return commandDescriptors.values();
    }

    public CommandDescriptor getCommandDescriptor(final String id) {
        assert id != null;

        return commandDescriptors.get(id);
    }
}