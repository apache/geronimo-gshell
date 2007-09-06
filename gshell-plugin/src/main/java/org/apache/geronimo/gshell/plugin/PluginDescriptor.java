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

import java.util.List;

import org.apache.geronimo.gshell.plugin.model.Command;
import org.codehaus.plexus.component.composition.Requirement;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;

/**
 * Container for additional component information for GShell plugins.
 *
 * @version $Rev$ $Date$
 */
public class PluginDescriptor
    extends ComponentSetDescriptor
{
    private String name;

    private String description;

    private String comment;

    private List<Command> commands;

    private Object configuration;
    
    private List<Requirement> requirements;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        assert name != null;
        
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public void setCommands(final List<Command> commands) {
        assert name != null;
        
        this.commands = commands;
    }

    public Object getConfiguration() {
        return configuration;
    }

    public void setConfiguration(final Object configuration) {
        this.configuration = configuration;
    }

    public List<Requirement> getRequirements() {
        return requirements;
    }

    public void setRequirements(final List<Requirement> requirements) {
        assert name != null;
        
        this.requirements = requirements;
    }

    public void addCommandDescriptor(final CommandDescriptor desc) {
        assert desc != null;

        addComponentDescriptor(desc);
    }
}