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
import org.apache.geronimo.gshell.command.CommandContainerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

/**
 * Default implementation of a {@link CommandContainerFactory} and {@link CommandContainerRegistry} components.
 *
 * @version $Rev$ $Date$
 */
public class CommandContainerFactoryImpl
    implements CommandContainerRegistry, CommandContainerFactory
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    // CommandContainerRegistry

    private Map<String,CommandContainer> registrations = new HashMap<String,CommandContainer>();

    public void register(final CommandContainer commandContainer) {
        assert commandContainer != null;

        String id = commandContainer.getId();

        log.debug("Registering command container: {}", id);
        
        registrations.put(id, commandContainer);
    }

    // CommandContainerFactory

    public CommandContainer create(final String id) throws Exception {
        assert id != null;

        log.debug("Locating container for ID: {}", id);

        CommandContainer commandContainer = registrations.get(id);

        if (commandContainer == null) {
            throw new RuntimeException("No command container registered for id: " + id);
        }

        return commandContainer;
    }
}