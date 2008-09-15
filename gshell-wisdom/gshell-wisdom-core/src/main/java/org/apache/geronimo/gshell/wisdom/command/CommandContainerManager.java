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
import org.apache.geronimo.gshell.command.CommandContainerResolver;
import org.apache.geronimo.gshell.command.CommandException;
import org.apache.geronimo.gshell.command.CommandNotFoundException;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.spring.BeanContainerAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides management of {@link CommandContainer} instances.
 *
 * @version $Rev$ $Date$
 */
public class CommandContainerManager
    implements BeanContainerAware, CommandContainerRegistry, CommandContainerFactory, CommandContainerResolver
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private BeanContainer beanContainer;
    
    private Map<String,CommandContainer> registrations = new HashMap<String,CommandContainer>();

    //
    // BeanContainerAware
    //
    
    public void setBeanContainer(final BeanContainer container) {
        assert container != null;

        this.beanContainer = container;
    }

    //
    // CommandContainerRegistry
    //

    public void register(final CommandContainer container) {
        assert container != null;

        String id = container.getId();

        log.debug("Registering command container: {}", id);
        
        registrations.put(id, container);

        beanContainer.publish(new CommandContainerRegisteredEvent(this, container));
    }

    //
    // CommandContainerFactory
    //

    public CommandContainer create(final String id) throws Exception {
        assert id != null;

        log.debug("Locating container for ID: {}", id);

        CommandContainer container = registrations.get(id);

        if (container == null) {
            throw new RuntimeException("No command container registered for id: " + id);
        }

        return container;
    }

    //
    // CommandContainerResolver
    //

    public CommandContainer resolve(final Variables variables, final String path) throws CommandException {
        assert variables != null;
        assert path != null;

        log.debug("Resolving container for path: {}", path);

        // HACK: For now, there is no nested muck, just use the name
        for (CommandContainer container : registrations.values()) {
            if (path.equals(container.getDocumenter().getName())) {
                return container;
            }
        }
        
        throw new CommandNotFoundException(path);
    }
}