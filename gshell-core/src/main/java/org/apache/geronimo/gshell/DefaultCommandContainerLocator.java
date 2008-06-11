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

package org.apache.geronimo.gshell;

import org.apache.geronimo.gshell.command.CommandContainer;
import org.apache.geronimo.gshell.plexus.GShellPlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.PlexusConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of a {@link CommandContainer.Locator} component.
 *
 * @version $Rev$ $Date$
 */
@Component(role=CommandContainer.Locator.class)
public class DefaultCommandContainerLocator
    implements CommandContainer.Locator, Contextualizable
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private GShellPlexusContainer container;

    public void contextualize(final Context context) throws ContextException {
        assert context != null;

        container = (GShellPlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
        assert container != null;

        log.debug("Container: {}", container);
    }

    public CommandContainer locate(final String id) {
        assert id != null;

        log.debug("Locating container for ID: {}", id);

        ComponentDescriptor descriptor = container.getComponentDescriptor(CommandContainer.class, id);
        if (descriptor == null) {
            // TODO: Throw typed exception
            throw new RuntimeException("Command container not found for ID: " + id);
        }

        CommandContainer commandContainer;
        try {
            commandContainer = container.lookupComponent(CommandContainer.class, id);
        }
        catch (ComponentLookupException e) {
            // TODO: Throw typed exception
            throw new RuntimeException("Failed to access command containe for ID: " + id, e);
        }

        return commandContainer;
    }
}