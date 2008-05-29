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
import org.apache.geronimo.gshell.model.common.Dependency;
import org.apache.geronimo.gshell.model.common.DependencyGroup;
import org.apache.geronimo.gshell.model.common.DescriptorSupport;
import org.apache.geronimo.gshell.model.common.LocalRepository;
import org.apache.geronimo.gshell.model.common.RemoteRepository;
import org.apache.geronimo.gshell.model.layout.Layout;

import java.util.ArrayList;
import java.util.List;

/**
 * Application model root element.
 *
 * @version $Rev$ $Date$
 */
@XStreamAlias("application")
public class Application
    extends DescriptorSupport
{
    private LocalRepository localRepository;

    private List<RemoteRepository> remoteRepositories;

    private List<Dependency> dependencies;

    private List<DependencyGroup> dependencyGroups;
    
    private Branding branding;

    private Layout layout;

    public LocalRepository getLocalRepository() {
        return localRepository;
    }

    public void setLocalRepository(final LocalRepository localRepository) {
        this.localRepository = localRepository;
    }

    public List<RemoteRepository> remoteRepositories() {
        if (remoteRepositories == null) {
            remoteRepositories = new ArrayList<RemoteRepository>();
        }

        return remoteRepositories;
    }

    public void add(final RemoteRepository repository) {
        assert repository != null;

        remoteRepositories().add(repository);
    }

    public List<DependencyGroup> dependencyGroups() {
        if (dependencyGroups == null) {
            dependencyGroups = new ArrayList<DependencyGroup>();
        }

        return dependencyGroups;
    }

    public void add(final DependencyGroup group) {
        assert group != null;

        dependencyGroups().add(group);
    }

    public List<Dependency> dependencies() {
        if (dependencies == null) {
            dependencies = new ArrayList<Dependency>();
        }

        return dependencies;
    }

    public List<Dependency> dependencies(boolean includeGroups) {
        if (!includeGroups) {
            return dependencies();
        }

        List<Dependency> list = new ArrayList<Dependency>();

        list.addAll(dependencies());

        for (DependencyGroup group : dependencyGroups()) {
            list.addAll(group.dependencies());
        }

        return list;
    }

    public void add(final Dependency dependency) {
        assert dependency != null;

        dependencies().add(dependency);
    }

    public Branding getBranding() {
        if (branding == null) {
            throw new IllegalStateException("Missing 'branding' configuration");
        }
        
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