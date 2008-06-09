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

package org.apache.geronimo.gshell.model.command;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.geronimo.gshell.model.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a command.
 *
 * @version $Rev$ $Date$
 */
@XStreamAlias("command")
public class Command
    extends Element
{
    private String id;

    private String implementation;

    private String description;

    private String version;

    //
    // TODO: Add containerClass (and/or container w/nested configuration?)
    //

    //
    // FIXME: Parameters and requirements are very plexus specific, and can be handled automatically by having
    //        the components gleaned while loading.
    //
    
    private List<Parameter> parameters;

    private List<Requirement> requirements;

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

    public List<Parameter> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<Parameter>();
        }
        return parameters;
    }

    public void setParameters(final List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(final Parameter parameter) {
        assert parameter != null;

        if (parameters == null) {
            parameters = new ArrayList<Parameter>();
        }

        parameters.add(parameter);
    }

    public boolean hasParameters() {
        return parameters != null;
    }

    public List<Requirement> getRequirements() {
        if (requirements == null) {
            requirements = new ArrayList<Requirement>();
        }
        return requirements;
    }

    public void setRequirements(final List<Requirement> requirements) {
        this.requirements = requirements;
    }

    public void addRequirement(final Requirement requirement) {
        assert requirement != null;

        if (requirements == null) {
            requirements = new ArrayList<Requirement>();
        }

        requirements.add(requirement);
    }

    public boolean hasRequirements() {
        return requirements != null;
    }
}