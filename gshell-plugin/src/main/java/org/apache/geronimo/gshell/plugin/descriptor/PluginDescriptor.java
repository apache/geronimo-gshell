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

package org.apache.geronimo.gshell.plugin.descriptor;

import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.model.command.Command;
import org.apache.geronimo.gshell.model.plugin.Plugin;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;

/**
 * Descriptor for a GShell plugin's plexus component set.
 *
 * @version $Rev$ $Date$
 */
public class PluginDescriptor
    extends ComponentSetDescriptor
{
    private final Plugin plugin;

    public PluginDescriptor(final Plugin plugin) {
        assert plugin != null;

        this.plugin = plugin;

        setId(plugin.getId());

        setIsolatedRealm(false);

        for (Command command : plugin.commands()) {
            addCommand(command);
        }
    }

    private void addCommand(final Command command) {
        assert command != null;
        
        CommandDescriptor descriptor = new CommandDescriptor(command);
        descriptor.setComponentSetDescriptor(this);
        descriptor.setSource(getSource());

        addComponentDescriptor(descriptor);
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}