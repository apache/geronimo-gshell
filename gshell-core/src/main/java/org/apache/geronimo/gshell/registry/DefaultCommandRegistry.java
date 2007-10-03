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

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.descriptor.CommandDescriptor;
import org.codehaus.plexus.component.annotations.Component;
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
@Component(role=CommandRegistry.class, hint="default")
public class DefaultCommandRegistry
    implements CommandRegistry
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Map<String, Command> commands = new HashMap<String, Command>();

    public void register(final Command command) {
        assert command != null;

        String id = command.getId();

        if (commands.containsKey(id)) {
            log.error("Ignoring duplicate: {}", id);
        }
        else {
            commands.put(id, command);
            log.debug("Registered: {}", id);
        }
    }

    public void unregister(final Command command) {
        assert command != null;

        String id = command.getId();

        if (!commands.containsKey(id)) {
            log.error("Ignoring uregistered: {}", id);
        }
        else {
            commands.remove(id);
            log.debug("Unregistered: {}", id);
        }
    }

    public Command lookup(final String id) {
        assert id != null;

        return commands.get(id);
    }

    public Collection<Command> commands() {
        return Collections.unmodifiableCollection(commands.values());
    }
}