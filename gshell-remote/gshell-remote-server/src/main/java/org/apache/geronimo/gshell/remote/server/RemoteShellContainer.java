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

package org.apache.geronimo.gshell.remote.server;

import java.util.UUID;

import org.apache.geronimo.gshell.whisper.session.SessionAttributeBinder;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;

/**
 * Extended Plexus container to access internals to manage state as needed.
 *
 * @version $Rev$ $Date$
 */
public class RemoteShellContainer
    extends DefaultPlexusContainer
{
    public static final SessionAttributeBinder<RemoteShellContainer> BINDER = new SessionAttributeBinder<RemoteShellContainer>(RemoteShellContainer.class);

    public RemoteShellContainer(final ContainerConfiguration config) throws PlexusContainerException {
        super(config);
    }

    public void disposeAllComponents() {
        super.disposeAllComponents();
    }

    //
    // Factory Access
    //

    public static RemoteShellContainer create(final ClassWorld classWorld) throws PlexusContainerException {
        assert classWorld != null;

        //
        // TODO: Setup some more reasonable configuration, like logging settings and such...
        //       also may want to provide a plexus.xml for this puppy to read er something?
        //
        
        ContainerConfiguration config = new DefaultContainerConfiguration();
        config.setName("gshell.remote-shell:" + UUID.randomUUID());
        config.setClassWorld(classWorld);

        return new RemoteShellContainer(config);
    }
}
