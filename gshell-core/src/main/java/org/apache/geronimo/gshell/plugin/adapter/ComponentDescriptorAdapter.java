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

package org.apache.geronimo.gshell.plugin.adapter;

import java.net.URI;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.descriptor.CommandDescriptor;
import org.apache.geronimo.gshell.descriptor.CommandRequirement;
import org.codehaus.plexus.component.repository.ComponentDescriptor;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class ComponentDescriptorAdapter
    extends ComponentDescriptor
{
    private final CommandDescriptor command;

    public ComponentDescriptorAdapter(final CommandDescriptor command) {
        assert command != null;

        this.command = command;

        URI source = command.getSource();
        
        if (source != null) {
            setSource(source.toString());
        }

        setDescription(command.getDescription());

        setAlias(null);

        setRole(Command.class.getName());

        setRoleHint(command.getId());

        setImplementation(command.getImplementation());

        setVersion(command.getVersion());

        setComponentType(null);

        setLifecycleHandler(null);

        setComponentProfile(null);

        setComponentFactory(null);

        setComponentComposer(null);

        setComponentConfigurator(null);

        setRealmId(null);

        setIsolatedRealm(false);

        setInstantiationStrategy("per-lookup");

        if (command.hasConfiguration()) {
            setConfiguration(new PlexusConfigurationAdapter(command.getConfiguration()));
        }

        if (command.hasRequirements()) {
            for (CommandRequirement requirement : command.getRequirements()) {
                this.addRequirement(new ComponentRequirementAdapter(requirement));
            }
        }

        //
        // TODO: What to do about depencencies?  Or are they just on the set level?
        //
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
    
    public CommandDescriptor getCommand() {
        return command;
    }
}