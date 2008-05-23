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

package org.apache.geronimo.gshell.registry;

import org.apache.geronimo.gshell.model.command.Command;
import org.apache.geronimo.gshell.plugin.CommandCollector;
import org.apache.geronimo.gshell.plugin.PlexusCommandWrapper;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Registers command components as they are discovered by the container.
 *
 * @version $Rev$ $Date$
 */
@Component(role=CommandRegistry.class)
public class DefaultCommandRegistry
    implements CommandRegistry, Contextualizable, Initializable
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private PlexusContainer container;

    @Requirement
    private CommandCollector collector;
    
    private Map<String, org.apache.geronimo.gshell.command.Command> commands = new HashMap<String, org.apache.geronimo.gshell.command.Command>();

    public void contextualize(Context context) throws ContextException {
        assert context != null;

        container = (PlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
        assert container != null;
        log.debug("Container: {}", container);
    }

    public void initialize() throws InitializationException {
        assert collector != null;
        log.debug("Collector: {}", collector);
    }

    public void register(final org.apache.geronimo.gshell.command.Command command) throws DuplicateRegistrationException {
        assert command != null;

        String id = command.getId();

        if (commands.containsKey(id)) {
            throw new DuplicateRegistrationException(id);
        }

        commands.put(id, command);
        log.debug("Registered: {}", id);
    }

    private void ensureRegistered(final String id) throws RegistryException {
        assert id != null;
        
        if (!commands.containsKey(id)) {
            Command descriptor = collector.getCommandDescriptor(id);

            if (descriptor == null) {
                throw new NotRegisteredException(id);
            }

            log.debug("Registering command id: {}", id);
            
            register(new PlexusCommandWrapper(container, descriptor));
        }
    }

    public void unregister(final org.apache.geronimo.gshell.command.Command command) throws RegistryException {
        assert command != null;

        String id = command.getId();

        ensureRegistered(id);

        commands.remove(id);
        log.debug("Unregistered: {}", id);
    }

    public org.apache.geronimo.gshell.command.Command lookup(final String id) throws RegistryException {
        assert id != null;

        ensureRegistered(id);

        return commands.get(id);
    }

    public Collection<org.apache.geronimo.gshell.command.Command> commands() {
        return Collections.unmodifiableCollection(commands.values());
    }
}