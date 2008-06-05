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
import org.apache.geronimo.gshell.model.command.Parameter;
import org.apache.geronimo.gshell.model.command.Requirement;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;

/**
 * Descriptor for a GShell command's plexus component.
 *
 * @version $Rev$ $Date$
 */
public class CommandDescriptor
    extends ComponentDescriptorSupport
{
    private final Command command;

    public CommandDescriptor(final Command command) {
        assert command != null;

        this.command = command;

        setDescription(command.getDescription());
        setRole(org.apache.geronimo.gshell.command.Command.class);
        setRoleHint(command.getId());
        setImplementation(command.getImplementation());
        setVersion(command.getVersion());
        setIsolatedRealm(false);
        setInstantiationStrategy("per-lookup");

        if (command.hasParameters()) {
            XmlPlexusConfiguration root = new XmlPlexusConfiguration("configuration");

            for (Parameter param : command.getParameters()) {
                root.addChild(new XmlPlexusConfiguration(param.getName(), param.getValue()));
            }

            setConfiguration(root);
        }

        if (command.hasRequirements()) {
            for (Requirement requirement : command.getRequirements()) {
                addRequirement(translate(requirement));
            }
        }
    }

    public Command getCommand() {
        return command;
    }

    private ComponentRequirement translate(final Requirement source) {
        assert source != null;

        ComponentRequirement requirement = new ComponentRequirement();

        requirement.setRole(source.getType());
        requirement.setRoleHint(source.getId());
        requirement.setFieldName(source.getName());
        requirement.setFieldMappingType(null);

        return requirement;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}