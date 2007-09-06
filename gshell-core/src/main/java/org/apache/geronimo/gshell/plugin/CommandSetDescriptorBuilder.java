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

import java.io.Reader;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.descriptor.CommandDescriptor;
import org.apache.geronimo.gshell.command.descriptor.CommandSetDescriptor;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.component.repository.io.PlexusTools;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper to build {@link CommandSetDescriptor} instances from configuration stream.
 *
 * @version $Rev$ $Date$
 */
public class CommandSetDescriptorBuilder
{
    private Logger log = LoggerFactory.getLogger(getClass());
    
    public CommandSetDescriptor build(final Reader reader, final String source) throws PlexusConfigurationException {
        assert reader != null;
        assert source != null;

        PlexusConfiguration c = PlexusTools.buildConfiguration(source, reader);

        CommandSetDescriptor setDescriptor = new CommandSetDescriptor();

        setDescriptor.setId(c.getChild("id").getValue());
        setDescriptor.setDescription(c.getChild("description").getValue());

        log.debug("Loading command set: {}", setDescriptor.getId());

        // Commands
        PlexusConfiguration[] commands = c.getChild("commands").getChildren("command");

        for (PlexusConfiguration command : commands) {
            CommandDescriptor d = createCommandDescriptor(command);
            setDescriptor.addCommandDescriptor(d);
        }
        
        return setDescriptor;
    }

    private CommandDescriptor createCommandDescriptor(final PlexusConfiguration c) throws PlexusConfigurationException {
        assert c != null;

        CommandDescriptor d = new CommandDescriptor();

        d.setId(c.getChild("id").getValue());
        d.setDescription(c.getChild("description").getValue());
        d.setImplementation(c.getChild("implementation").getValue());

        log.debug("Loading command: {}", d.getId());

        d.setRole(Command.class.getName());
        d.setRoleHint(d.getId());
        d.setInstantiationStrategy("per-lookup");
        
        // Requirements
        PlexusConfiguration[] requirements = c.getChild("requirements").getChildren("requirement");

        for (PlexusConfiguration requirement : requirements) {
            ComponentRequirement r = new ComponentRequirement();
            
            r.setRole(requirement.getChild("role").getValue());
            r.setRoleHint(requirement.getChild("role-hint").getValue());
            r.setFieldName(requirement.getChild("field-name").getValue());

            d.addRequirement(r);
        }

        return d;
    }
}