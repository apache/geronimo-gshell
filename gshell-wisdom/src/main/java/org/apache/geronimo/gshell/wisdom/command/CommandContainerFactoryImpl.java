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

package org.apache.geronimo.gshell.wisdom.command;

import org.apache.geronimo.gshell.command.CommandContainer;
import org.apache.geronimo.gshell.command.CommandContainerFactory;
import org.apache.geronimo.gshell.plexus.GShellPlexusContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of a {@link CommandContainerFactory} component.
 *
 * @version $Rev$ $Date$
 */
public class CommandContainerFactoryImpl
    implements CommandContainerFactory
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private GShellPlexusContainer container;

    public CommandContainer create(final String id) throws Exception {
        assert id != null;

        log.debug("Locating container for ID: {}", id);

        /*
        ComponentDescriptor descriptor = container.getComponentDescriptor(CommandContainer.class, id);
        if (descriptor == null) {
            // TODO: Throw typed exception
            throw new Exception("Command container not found for ID: " + id);
        }

        CommandContainer command;
        try {
            command = container.lookupComponent(CommandContainer.class, id);
        }
        catch (ComponentLookupException e) {
            // TODO: Throw typed exception
            throw new Exception("Failed to access command container for ID: " + id, e);
        }

        return command;
        */

        return null;
    }
}