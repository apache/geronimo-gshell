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

package org.apache.geronimo.gshell.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Application model root element.
 *
 * @version $Rev$ $Date$
 */
public class ApplicationModel
    extends Model
{
    private String groupId;

    private String artifactId;

    private String version;

    private String name;

    private String description;

    private Properties properties;

    private List<Artifact> dependencies;

    private List<Artifact> plugins;

    private Branding branding;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(final String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(final String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getId() {
        return getGroupId() + ":" + getArtifactId() + ":" + getVersion();
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
        if (properties == null) {
            properties = new Properties();
        }

        return properties;
    }

    public void setProperties(final Properties properties) {
        this.properties = properties;
    }

    // DependencyArtifact

    public List<Artifact> getDependencies() {
        if (dependencies == null) {
            dependencies = new ArrayList<Artifact>();
        }

        return dependencies;
    }

    public void setDependencies(final List<Artifact> dependencies) {
        this.dependencies = dependencies;
    }

    public void addDependency(final Artifact dependency) {
        assert dependency != null;

        getDependencies().add(dependency);
    }

    // PluginArtifact

    public List<Artifact> getPlugins() {
        if (plugins == null) {
            plugins = new ArrayList<Artifact>();
        }

        return plugins;
    }

    public void setPlugins(final List<Artifact> plugins) {
        this.plugins = plugins;
    }

    public void addPlugin(final Artifact plugin) {
        assert plugin != null;

        getPlugins().add(plugin);
    }

    // Branding

    public Branding getBranding() {
        if (branding == null) {
            throw new IllegalStateException("Missing 'branding' configuration");
        }
        
        return branding;
    }

    public void setBranding(final Branding branding) {
        this.branding = branding;

        // HACK: Hookup parent
        branding.setParent(this);
    }
    
    /**
     * Link children to their parent when deserializing.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    private Object readResolve() {
        if (branding != null) {
            branding.setParent(this);
        }

        return this;
    }
}