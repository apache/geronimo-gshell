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

package org.apache.geronimo.gshell.model.plugin;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.geronimo.gshell.model.common.ModelRoot;
import org.apache.geronimo.gshell.model.common.DependencyGroup;
import org.apache.geronimo.gshell.model.common.Dependency;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Plugin model root element.
 *
 * @version $Rev$ $Date$
 */
@XStreamAlias("plugin")
public class Plugin
    extends ModelRoot
{
    private String id;

    // groupId

    // artifactId

    // version

    private String name;

    private String description;

    private Properties properties;
    
    private List<Dependency> dependencies = new ArrayList<Dependency>();

    private List<DependencyGroup> dependencyGroups = new ArrayList<DependencyGroup>();

    // Commands
    
    // Layout

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(final Properties properties) {
        this.properties = properties;
    }

    public List<Dependency> dependencies() {
        return dependencies;
    }

    public void add(final Dependency dependency) {
        assert dependency != null;

        if (dependencies == null) {
            dependencies = new ArrayList<Dependency>();
        }

        dependencies.add(dependency);
    }

    public List<DependencyGroup> dependencyGroups() {
        return dependencyGroups;
    }

    public void add(final DependencyGroup group) {
        assert group != null;

        if (dependencyGroups == null) {
            dependencyGroups = new ArrayList<DependencyGroup>();
        }

        dependencyGroups.add(group);
    }
}