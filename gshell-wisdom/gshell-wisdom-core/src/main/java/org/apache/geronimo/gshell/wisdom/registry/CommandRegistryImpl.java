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

package org.apache.geronimo.gshell.wisdom.registry;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.event.EventPublisher;
import org.apache.geronimo.gshell.registry.CommandRegistry;
import org.apache.geronimo.gshell.registry.NoSuchCommandException;
import org.apache.geronimo.gshell.registry.DuplicateCommandException;
import org.apache.geronimo.gshell.vfs.provider.meta.MetaDataRegistry;
import org.apache.geronimo.gshell.vfs.provider.meta.MetaDataRegistryConfigurer;
import org.apache.geronimo.gshell.vfs.provider.meta.MetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link CommandRegistry} component.
 *
 * @version $Rev$ $Date$
 */
public class CommandRegistryImpl
    implements CommandRegistry
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private MetaDataRegistry metaRegistry;

    private MetaDataRegistryConfigurer metaConfig;

    private final Map<String,Command> commands = new LinkedHashMap<String,Command>();

    @PostConstruct
    public void init() {
        assert metaRegistry != null;
        metaConfig = new MetaDataRegistryConfigurer(metaRegistry);
        metaConfig.addFolder("/commands");
    }

    public void registerCommand(final String name, final Command command) throws DuplicateCommandException {
        assert name != null;
        assert command != null;

        log.debug("Registering command: {} -> {}", name, command);

        if (containsCommand(name)) {
            throw new DuplicateCommandException(name);
        }

        assert metaConfig != null;
        MetaData data = metaConfig.addFile("/commands/" + name);
        data.addAttribute("COMMAND", command);

        commands.put(name, command);

        eventPublisher.publish(new CommandRegisteredEvent(name, command));
    }

    public void removeCommand(final String name) throws NoSuchCommandException {
        assert name != null;

        log.debug("Removing command: {}", name);

        if (!containsCommand(name)) {
            throw new NoSuchCommandException(name);
        }

        // TODO: Remove from meta
        
        commands.remove(name);

        eventPublisher.publish(new CommandRemovedEvent(name));
    }

    public Command getCommand(final String name) throws NoSuchCommandException {
        assert name != null;

        if (!containsCommand(name)) {
            throw new NoSuchCommandException(name);
        }

        return commands.get(name);
    }

    public boolean containsCommand(final String name) {
        assert name != null;

        return commands.containsKey(name);
    }

    public Collection<String> getCommandNames() {
        return Collections.unmodifiableSet(commands.keySet());
    }
}