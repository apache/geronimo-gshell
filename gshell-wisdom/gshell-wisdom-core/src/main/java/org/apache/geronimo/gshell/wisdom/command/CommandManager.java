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
import org.apache.geronimo.gshell.command.CommandException;
import org.apache.geronimo.gshell.command.CommandNotFoundException;
import org.apache.geronimo.gshell.command.CommandRegistration;
import org.apache.geronimo.gshell.command.CommandRegistry;
import org.apache.geronimo.gshell.command.CommandResolver;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.event.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    private List<CommandRegistration> registrations = new ArrayList<CommandRegistration>();

    //
    // CommandRegistry
    //

    public CommandRegistration register(final Command command) {
        assert command != null;

        log.debug("Registering command: {}", command);

        CommandRegistration registration = new CommandRegistration() {
            public Command getCommand() {
                return command;
            }
        };

        registrations.add(registration);

        eventPublisher.publish(new CommandRegisteredEvent(registration));

        return registration;
    }

    public Collection<CommandRegistration> getRegistrations() {
        return Collections.unmodifiableCollection(registrations);
    }

    //
    // CommandResolver
    //

    public Command resolve(final Variables variables, final String path) throws CommandException {
        assert variables != null;
        assert path != null;

        log.debug("Resolving command for path: {}", path);

        // HACK: For now, there is no nested muck, just use the name
        for (CommandRegistration registration : registrations) {
            if (path.equals(registration.getCommand().getDocumenter().getName())) {
                return registration.getCommand();
            }
        }
        
        throw new CommandNotFoundException(path);
    }
}