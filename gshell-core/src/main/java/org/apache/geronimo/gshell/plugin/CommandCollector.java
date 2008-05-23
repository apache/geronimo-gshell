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
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryEvent;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryListener;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps command ids to command descriptors for just in time component initialization.
 *
 * This is not a typical Plexus component and can not have any requirements or custom configuration
 * due to how the container hacks this puppy into existance.  This component is bound to a role
 * which is the same as its classname.
 *
 * @version $Rev$ $Date$
 */
@Component(role= CommandCollector.class)
public class CommandCollector
    implements ComponentDiscoveryListener
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Map<String,CommandDescriptor> descriptors = new HashMap<String,CommandDescriptor>();

    public CommandCollector() {
        log.trace("Created: {}", this);
    }

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
                log.debug("Found: {}", descriptor);

                descriptors.put(descriptor.getId(), descriptor);
            }
        }
    }

    public CommandDescriptor getCommandDescriptor(final String id) {
        assert id != null;

        log.debug("Lookup descriptor for ID: {}", id);
        
        return descriptors.get(id);
    }
}