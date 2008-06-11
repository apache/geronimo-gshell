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

package org.apache.geronimo.gshell.plexus;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DuplicateChildContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

/**
 * And extention of the default Plexus container.
 *
 * @version $Rev$ $Date$
 */
public class GShellPlexusContainer
    extends DefaultPlexusContainer
{
    public GShellPlexusContainer() throws PlexusContainerException {
        super();
    }

    public GShellPlexusContainer(final ContainerConfiguration config) throws PlexusContainerException {
        super(config);
    }

    public GShellPlexusContainer createChild(final String name, final ClassRealm realm) throws PlexusContainerException {
        assert name != null;
        assert realm != null;

        if (hasChildContainer(name)) {
            throw new DuplicateChildContainerException(getName(), name);
        }

        ContainerConfiguration config = new DefaultContainerConfiguration();
        config.setName(name);
        config.setParentContainer(this);
        config.setClassWorld(new ClassWorld(name, realm));

        GShellPlexusContainer childContainer = new GShellPlexusContainer(config);

        //noinspection unchecked
        childContainers.put(name, childContainer);

        return childContainer;
    }

    public GShellPlexusContainer createChild(final ContainerConfiguration config) throws PlexusContainerException {
        assert config != null;

        String name = config.getName();

        if (hasChildContainer(name)) {
            throw new DuplicateChildContainerException(getName(), name);
        }

        config.setParentContainer(this);

        GShellPlexusContainer childContainer = new GShellPlexusContainer(config);

        //noinspection unchecked
        childContainers.put(name, childContainer);

        return childContainer;
    }

    @Override
    public PlexusContainer createChildContainer(final String name, final ClassRealm realm) throws PlexusContainerException {
        return createChild(name, realm);
    }

    //
    // Add type-safe lookups based on class roles
    //
    
    @SuppressWarnings("unchecked")
    public <T> T lookupComponent(final Class<T> role) throws ComponentLookupException {
        assert role != null;

        return (T) super.lookup(role);
    }

    @SuppressWarnings("unchecked")
    public <T> T lookupComponent(final Class<T> role, final String roleHint) throws ComponentLookupException {
        assert role != null;
        assert roleHint != null;

        return (T) super.lookup(role, roleHint);
    }

    //
    // TODO: lookupComponentList, lookupComponentMap, createChildContainer
    //

    public ComponentDescriptor getComponentDescriptor(final Class role, final String hint) {
        assert role != null;
        assert hint != null;

        return getComponentDescriptor(role.getName(), hint);
    }
}
