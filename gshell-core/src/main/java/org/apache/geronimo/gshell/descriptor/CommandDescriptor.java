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

package org.apache.geronimo.gshell.descriptor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;

/**
 * Describes a command.
 *
 * @version $Rev$ $Date$
 */
@XStreamAlias("command")
public class CommandDescriptor
{
    private URI source;

    private String id;

    private String implementation;

    private String description;

    private String version;

    private List<CommandParameter> parameters;

    private List<CommandRequirement> requirements;

    private List<CommandDependency> dependencies;

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public URI getSource() {
        return source;
    }

    public void setSource(final URI source) {
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getImplementation() {
        return implementation;
    }

    public void setImplementation(final String implementation) {
        this.implementation = implementation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public List<CommandParameter> getParameters() {
        return parameters;
    }

    public void setParameters(final List<CommandParameter> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(final CommandParameter parameter) {
        assert parameter != null;

        if (parameters == null) {
            parameters = new ArrayList<CommandParameter>();
        }

        parameters.add(parameter);
    }

    public boolean hasParameters() {
        return parameters != null;
    }

    public List<CommandRequirement> getRequirements() {
        return requirements;
    }

    public void setRequirements(final List<CommandRequirement> requirements) {
        this.requirements = requirements;
    }

    public void addRequirement(final CommandRequirement requirement) {
        assert requirement != null;

        if (requirements == null) {
            requirements = new ArrayList<CommandRequirement>();
        }

        requirements.add(requirement);
    }

    public boolean hasRequirements() {
        return requirements != null;
    }

    public List<CommandDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(final List<CommandDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public void addDependency(final CommandDependency dependency) {
        assert dependency != null;

        if (dependencies == null) {
            dependencies = new ArrayList<CommandDependency>();
        }

        dependencies.add(dependency);
    }

    public boolean hasDependencies() {
        return dependencies != null;
    }
}