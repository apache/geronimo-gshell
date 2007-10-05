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

import org.apache.geronimo.gshell.descriptor.CommandDescriptor;
import org.apache.geronimo.gshell.descriptor.CommandSetDescriptor;
import org.apache.geronimo.gshell.plugin.adapter.ComponentSetDescriptorAdapter;
import org.apache.geronimo.gshell.registry.CommandRegistry;
import org.apache.geronimo.gshell.registry.DuplicateRegistrationException;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryEvent;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryListener;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers commands with the registry as they are discovered by the container.
 * 
 * @version $Rev$ $Date$
 */
@Component(role=ComponentDiscoveryListener.class, hint="command")
public class CommandDiscoveryListener
    implements ComponentDiscoveryListener
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private PlexusContainer container;

    @Requirement
    private CommandRegistry registry;

    public String getId() {
        return getClass().getSimpleName();
    }
    
    public void componentDiscovered(final ComponentDiscoveryEvent event) {
        assert event != null;

        log.trace("Event: {}", event);

        ComponentSetDescriptor components = event.getComponentSetDescriptor();

        if (components instanceof ComponentSetDescriptorAdapter) {
            CommandSetDescriptor commands = ((ComponentSetDescriptorAdapter)components).getCommands();

            for (CommandDescriptor descriptor : commands.getCommands()) {
                try {
                    registry.register(new PlexusCommandWrapper(container, descriptor));
                }
                catch (DuplicateRegistrationException e) {
                    log.error("Failed to register command: " + descriptor, e);
                }
            }
        }
    }
}