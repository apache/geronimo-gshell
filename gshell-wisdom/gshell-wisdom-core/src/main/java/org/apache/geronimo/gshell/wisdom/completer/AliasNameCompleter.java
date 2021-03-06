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

package org.apache.geronimo.gshell.wisdom.completer;

import jline.Completor;
import org.apache.geronimo.gshell.console.completer.StringsCompleter;
import org.apache.geronimo.gshell.event.Event;
import org.apache.geronimo.gshell.event.EventListener;
import org.apache.geronimo.gshell.event.EventManager;
import org.apache.geronimo.gshell.registry.AliasRegistry;
import org.apache.geronimo.gshell.wisdom.registry.AliasRegisteredEvent;
import org.apache.geronimo.gshell.wisdom.registry.AliasRemovedEvent;

import java.util.Collection;
import java.util.List;

/**
 * {@link Completor} for alias names.
 *
 * Keeps up to date automatically by handling alias-related events.
 *
 * @version $Rev$ $Date$
 */
public class AliasNameCompleter
    implements Completor
{
    private final EventManager eventManager;

    private final AliasRegistry aliasRegistry;

    private final StringsCompleter delegate = new StringsCompleter();

    public AliasNameCompleter(final EventManager eventManager, final AliasRegistry aliasRegistry) {
        assert eventManager != null;
        this.eventManager = eventManager;
        assert aliasRegistry != null;
        this.aliasRegistry = aliasRegistry;
    }

    // @PostConstruct
    public void init() {
        // Populate the initial list of alias names
        Collection<String> names = aliasRegistry.getAliasNames();
        delegate.getStrings().addAll(names);

        // Register for updates to alias registrations
        eventManager.addListener(new EventListener() {
            public void onEvent(final Event event) throws Exception {
                if (event instanceof AliasRegisteredEvent) {
                    AliasRegisteredEvent targetEvent = (AliasRegisteredEvent)event;
                    delegate.getStrings().add(targetEvent.getName());
                }
                else if (event instanceof AliasRemovedEvent) {
                    AliasRemovedEvent targetEvent = (AliasRemovedEvent)event;
                    delegate.getStrings().remove(targetEvent.getName());
                }
            }
        });
    }

    public int complete(final String buffer, final int cursor, final List candidates) {
        return delegate.complete(buffer, cursor, candidates);
    }
}