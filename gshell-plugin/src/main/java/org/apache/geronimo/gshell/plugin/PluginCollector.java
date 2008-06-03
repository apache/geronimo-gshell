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

import org.apache.geronimo.gshell.model.command.Command;
import org.apache.geronimo.gshell.model.plugin.Plugin;
import org.apache.geronimo.gshell.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryEvent;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryListener;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Collects discovered GShell plugins and makes them available for registry lookups.
 *
 * @version $Rev$ $Date$
 */
@Component(role=PluginCollector.class)
public class PluginCollector
    implements ComponentDiscoveryListener
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final List<PluginDescriptor> descriptors = new ArrayList<PluginDescriptor>();

    private final Map<String,Command> commands = new LinkedHashMap<String,Command>();

    public String getId() {
        return getClass().getSimpleName();
    }

    public void componentDiscovered(final ComponentDiscoveryEvent event) {
        assert event != null;

        log.trace("Component discovered: {}", event);

        ComponentSetDescriptor descriptor = event.getComponentSetDescriptor();

        if (descriptor instanceof PluginDescriptor) {
            collect((PluginDescriptor)descriptor);
        }
    }

    private void collect(final PluginDescriptor descriptor) {
        assert descriptor != null;

        Plugin plugin = descriptor.getPlugin();

        log.trace("Collecting plugin components for: {}", plugin.getId());

        for (Command command : plugin.commands()) {
            log.debug("Found command: {}", command.getId());
            log.trace("Command model: {}", command);
            
            commands.put(command.getId(), command);
        }

        descriptors.add(descriptor);
    }

    public List<PluginDescriptor> getDescriptors() {
        return descriptors;
    }

    public Map<String, Command> getCommands() {
        return commands;
    }

    public Command getCommand(final String id) {
        assert id != null;

        return commands.get(id);
    }
}