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

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandRegistry;
import org.apache.geronimo.gshell.command.CommandResolver;
import org.apache.geronimo.gshell.command.CommandException;
import org.apache.geronimo.gshell.command.CommandNotFoundException;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.event.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides management of {@link Command} instances.
 *
 * @version $Rev$ $Date$
 */
public class CommandManager
    implements CommandRegistry, CommandResolver
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private EventPublisher eventPublisher;

    private Map<String, Command> registrations = new HashMap<String, Command>();

    //
    // CommandRegistry
    //

    public void register(final Command command) {
        assert command != null;

        String id = command.getId();

        log.debug("Registering command: {}", id);
        
        registrations.put(id, command);

        eventPublisher.publish(new CommandRegisteredEvent(command));
    }

    //
    // CommandResolver
    //

    public Command resolve(final Variables variables, final String path) throws CommandException {
        assert variables != null;
        assert path != null;

        log.debug("Resolving command for path: {}", path);

        // HACK: For now, there is no nested muck, just use the name
        for (Command command : registrations.values()) {
            if (path.equals(command.getDocumenter().getName())) {
                return command;
            }
        }
        
        throw new CommandNotFoundException(path);
    }
}