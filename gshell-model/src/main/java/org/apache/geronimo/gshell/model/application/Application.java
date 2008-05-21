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

package org.apache.geronimo.gshell.model.application;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.geronimo.gshell.model.common.ModelRoot;
import org.apache.geronimo.gshell.model.common.Dependency;
import org.apache.geronimo.gshell.model.common.DependencyGroup;
import org.apache.geronimo.gshell.model.common.SourceRepository;
import org.apache.geronimo.gshell.model.layout.Layout;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.io.File;

/**
 * Application model root element.
 *
 * @version $Rev$ $Date$
 */
@XStreamAlias("application")
public class Application
    extends ModelRoot
{
    private String id;

    private String name;

    private String description;

    private Properties properties;

    private File repository;

    private List<SourceRepository> sourceRepositories;

    private List<Dependency> dependencies;

    private List<DependencyGroup> dependencyGroups;
    
    private Branding branding;

    private Layout layout;

    // Paths

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

    public File getRepository() {
        return repository;
    }

    public void setRepository(final File repository) {
        this.repository = repository;
    }

    public List<SourceRepository> sourceRepositories() {
        return sourceRepositories;
    }

    public void add(final SourceRepository repository) {
        assert repository != null;

        if (sourceRepositories == null) {
            sourceRepositories = new ArrayList<SourceRepository>();
        }

        sourceRepositories.add(repository);
    }

    //
    // TODO: Change to Plugin*
    //

    //
    // TODO: Provide accessor to aggregate dependencies w/group dependencies
    //

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

    public Branding getBranding() {
        return branding;
    }

    public void setBranding(final Branding branding) {
        this.branding = branding;
    }

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(final Layout layout) {
        this.layout = layout;
    }
}