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

package org.apache.geronimo.gshell.support.plexus;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

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

    public GShellPlexusContainer(final ContainerConfiguration configuration) throws PlexusContainerException {
        super(configuration);
    }
    
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
}
