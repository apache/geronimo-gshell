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

package org.apache.geronimo.gshell.whisper.transport;

import java.net.URI;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * Helper to locate a {@link TransportFactory} instance.
 *
 * @version $Rev$ $Date$
 */
@Component(role=TransportFactoryLocator.class)
public class TransportFactoryLocator<T extends TransportFactory>
{
    @Requirement
    private PlexusContainer container;

    public PlexusContainer getContainer() {
        return container;
    }

    public void setContainer(final PlexusContainer container) {
        this.container = container;
    }

    public T locate(final URI location) throws InvalidLocationException, ComponentLookupException {
        assert location != null;

        String scheme = location.getScheme();

        if (scheme == null) {
            throw new InvalidLocationException(location);
        }

        // noinspection unchecked
        return (T) container.lookup(TransportFactory.class, scheme);
    }

}