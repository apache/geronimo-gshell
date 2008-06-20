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

package org.apache.geronimo.gshell.rapture;

import org.apache.geronimo.gshell.plexus.GShellPlexusContainer;
import org.apache.geronimo.gshell.command.CommandFactory;
import org.apache.geronimo.gshell.command.Command;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of a {@link CommandFactory} component.
 *
 * @version $Rev$ $Date$
 */
@Component(role=CommandFactory.class)
public class DefaultCommandFactory
    implements CommandFactory, Contextualizable
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private GShellPlexusContainer container;

    public void contextualize(final Context context) throws ContextException {
        assert context != null;

        container = (GShellPlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
        assert container != null;

        log.debug("Container: {}", container);
    }

    public Command create(final String id) throws Exception {
        assert id != null;

        log.debug("Locating container for ID: {}", id);

        ComponentDescriptor descriptor = container.getComponentDescriptor(Command.class, id);
        if (descriptor == null) {
            // TODO: Throw typed exception
            throw new Exception("Command container not found for ID: " + id);
        }

        Command command;
        try {
            command = container.lookupComponent(Command.class, id);
        }
        catch (ComponentLookupException e) {
            // TODO: Throw typed exception
            throw new Exception("Failed to access command container for ID: " + id, e);
        }

        return command;
    }
}