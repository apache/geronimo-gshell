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

package org.apache.geronimo.gshell.wisdom.alias;

import org.apache.geronimo.gshell.alias.Alias;
import org.apache.geronimo.gshell.alias.AliasManager;
import org.apache.geronimo.gshell.command.CommandRegistry;
import org.apache.geronimo.gshell.event.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link AliasManager} component.
 *
 * @version $Rev$ $Date$
 */
public class AliasManagerImpl
    implements AliasManager
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private CommandRegistry commandRegistry;

    @Autowired
    private EventPublisher eventPublisher;

    private Map<String,Alias> aliases = new LinkedHashMap<String,Alias>();

    public Collection<Alias> getAliases() {
        return Collections.unmodifiableCollection(aliases.values());
    }

    public boolean isAliasDefined(final String name) {
        assert name != null;

        return aliases.containsKey(name);
    }

    public Alias defineAlias(final String name, final String target) {
        assert name != null;

        log.debug("Defining alias: {} -> {}", name, target);

        Alias alias = new Alias() {
            public String getName() {
                return name;
            }

            public String getTarget() {
                return target;
            }
        };

        aliases.put(name, alias);

        // TODO: Register AliasCommand

        eventPublisher.publish(new AliasDefinedEvent(alias));

        return alias;
    }

    public void undefineAlias(final String name) {
        assert name != null;

        log.debug("Undefining alias: {}", name);

        Alias alias = aliases.remove(name);
        if (alias == null) {
            log.debug("No alias defined; ignoring");
        }
        else {
            // TODO: Unregister AliasCommand

            eventPublisher.publish(new AliasUndefinedEvent(alias));
        }
    }
}
