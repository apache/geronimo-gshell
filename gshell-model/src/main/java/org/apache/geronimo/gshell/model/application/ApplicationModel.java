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
import org.apache.geronimo.gshell.model.application.DependencyArtifact;
import org.apache.geronimo.gshell.model.application.DependencyGroup;
import org.apache.geronimo.gshell.model.common.DescriptorSupport;
import org.apache.geronimo.gshell.model.common.LocalRepository;
import org.apache.geronimo.gshell.model.common.RemoteRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Application model root element.
 *
 * @version $Rev$ $Date$
 */
@XStreamAlias("application")
public class ApplicationModel
    extends DescriptorSupport
{
    private LocalRepository localRepository;

    private List<RemoteRepository> remoteRepositories;

    private List<DependencyArtifact> dependencies;

    private List<DependencyGroup> dependencyGroups;

    private List<PluginArtifact> plugins;

    private List<PluginGroup> pluginGroups;

    private Branding branding;

    // LocalRepository

    public LocalRepository getLocalRepository() {
        return localRepository;
    }

    public void setLocalRepository(final LocalRepository localRepository) {
        this.localRepository = localRepository;
    }

    // RemoteRepository

    public List<RemoteRepository> getRemoteRepositories() {
        if (remoteRepositories == null) {
            remoteRepositories = new ArrayList<RemoteRepository>();
        }

        return remoteRepositories;
    }

    public void add(final RemoteRepository repository) {
        assert repository != null;

        getRemoteRepositories().add(repository);
    }

    // DependencyGroup

    public List<DependencyGroup> getDependencyGroups() {
        if (dependencyGroups == null) {
            dependencyGroups = new ArrayList<DependencyGroup>();
        }

        return dependencyGroups;
    }

    public void add(final DependencyGroup group) {
        assert group != null;

        getDependencyGroups().add(group);
    }

    // DependencyArtifact

    public List<DependencyArtifact> getDependencies() {
        if (dependencies == null) {
            dependencies = new ArrayList<DependencyArtifact>();
        }

        return dependencies;
    }

    public List<DependencyArtifact> getDependencies(boolean includeGroups) {
        if (!includeGroups) {
            return getDependencies();
        }

        List<DependencyArtifact> list = new ArrayList<DependencyArtifact>();

        list.addAll(getDependencies());

        for (DependencyGroup group : getDependencyGroups()) {
            list.addAll(group.getDependencies());
        }

        return list;
    }

    public void add(final DependencyArtifact dependency) {
        assert dependency != null;

        getDependencies().add(dependency);
    }

    // PluginGroup

    public List<PluginGroup> getPluginGroups() {
        if (pluginGroups == null) {
            pluginGroups = new ArrayList<PluginGroup>();
        }

        return pluginGroups;
    }

    public void add(final PluginGroup group) {
        assert group != null;

        getPluginGroups().add(group);
    }

    // PluginArtifact

    public List<PluginArtifact> getPlugins() {
        if (plugins == null) {
            plugins = new ArrayList<PluginArtifact>();
        }

        return plugins;
    }

    public List<PluginArtifact> getPlugins(boolean includeGroups) {
        if (!includeGroups) {
            return getPlugins();
        }

        List<PluginArtifact> list = new ArrayList<PluginArtifact>();

        list.addAll(getPlugins());

        for (PluginGroup group : getPluginGroups()) {
            list.addAll(group.getPlugins());
        }

        return list;
    }

    public void add(final PluginArtifact plugin) {
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