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

package org.apache.geronimo.gshell.wisdom.shell.completer;

import jline.ArgumentCompletor;
import jline.Completor;
import jline.NullCompletor;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.console.completer.MuxCompleter;
import org.apache.geronimo.gshell.console.completer.StringsCompleter;
import org.apache.geronimo.gshell.event.Event;
import org.apache.geronimo.gshell.event.EventListener;
import org.apache.geronimo.gshell.event.EventManager;
import org.apache.geronimo.gshell.registry.CommandRegistry;
import org.apache.geronimo.gshell.wisdom.registry.CommandRegisteredEvent;
import org.apache.geronimo.gshell.wisdom.registry.CommandRemovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link Completor} for commands, including support for command-specific sub-completion.
 *
 * @version $Rev$ $Date$
 */
public class CommandsCompleter
    implements Completor
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private EventManager eventManager;

    @Autowired
    private CommandRegistry commandRegistry;

    private final Map<String,Completor> completors = new HashMap<String,Completor>();

    private final MuxCompleter delegate = new MuxCompleter();

    @PostConstruct
    public void init() throws Exception {
        log.debug("Initializing");

        // Populate the initial list of completers from the currently registered commands
        Collection<String> names = commandRegistry.getCommandNames();
        for (String name : names) {
            Command command = commandRegistry.getCommand(name);
            addCompleter(name, command);
        }

        // Register for updates to command registrations
        eventManager.addListener(new EventListener() {
            public void onEvent(final Event event) throws Exception {
                if (event instanceof CommandRegisteredEvent) {
                    CommandRegisteredEvent targetEvent = (CommandRegisteredEvent)event;
                    addCompleter(targetEvent.getName(), targetEvent.getCommand());
                }
                else if (event instanceof CommandRemovedEvent) {
                    CommandRemovedEvent targetEvent = (CommandRemovedEvent)event;
                    removeCompleter(targetEvent.getName());
                }
            }
        });
    }

    private void addCompleter(final String name, final Command command) {
        assert name != null;
        assert command != null;

        List<Completor> children = new ArrayList<Completor>();

        // Attach completion for the command name
        children.add(new StringsCompleter(new String[] { name }));

        // Then attach any command specific completers
        Collection<Completor> commandCompleters = command.getCompleter().createCompletors();
        if (commandCompleters != null) {
            for (Completor completer : commandCompleters) {
                if (completer != null) {
                    children.add(completer);
                }
                else {
                    children.add(new NullCompletor());
                }
            }
        }
        else {
            children.add(new NullCompletor());
        }

        // Setup the root completer for the command
        Completor root = new ArgumentCompletor(children);

        // Track and attach
        completors.put(name, root);
        delegate.getCompleters().add(root);
    }

    private void removeCompleter(final String name) {
        assert name != null;

        Completor completer = completors.remove(name);
        delegate.getCompleters().remove(completer);
    }
    
    public int complete(final String buffer, final int cursor, final List candidates) {
        return delegate.complete(buffer, cursor, candidates);
    }
}