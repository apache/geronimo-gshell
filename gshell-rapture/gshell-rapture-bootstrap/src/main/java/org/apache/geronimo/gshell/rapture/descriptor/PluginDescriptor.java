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

package org.apache.geronimo.gshell.rapture.descriptor;

import org.apache.geronimo.gshell.yarn.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.yarn.ToStringStyle;
import org.apache.geronimo.gshell.model.command.CommandModel;
import org.apache.geronimo.gshell.model.plugin.Plugin;
import org.apache.geronimo.gshell.rapture.descriptor.CommandActionDescriptor;
import org.apache.geronimo.gshell.rapture.descriptor.CommandCompleterDescriptor;
import org.apache.geronimo.gshell.rapture.descriptor.CommandContainerDescriptor;
import org.apache.geronimo.gshell.rapture.descriptor.CommandDocumenterDescriptor;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.codehaus.plexus.component.repository.ComponentDescriptor;

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

        for (CommandModel model : plugin.getCommands()) {
            addCommand(model);
        }
    }

    private void addCommand(final CommandModel model) {
        assert model != null;

        addComponentDescriptor(new CommandActionDescriptor(model));
        addComponentDescriptor(new CommandDocumenterDescriptor(model));
        addComponentDescriptor(new CommandCompleterDescriptor(model));
        addComponentDescriptor(new CommandContainerDescriptor(model));
    }

    public void addComponentDescriptor(final ComponentDescriptor descriptor) {
        assert descriptor != null;

        descriptor.setComponentSetDescriptor(this);
        descriptor.setSource(getSource());

        super.addComponentDescriptor(descriptor);
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}