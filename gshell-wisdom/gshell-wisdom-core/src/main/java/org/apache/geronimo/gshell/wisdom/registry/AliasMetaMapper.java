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

import org.apache.geronimo.gshell.event.Event;
import org.apache.geronimo.gshell.event.EventListener;
import org.apache.geronimo.gshell.event.EventManager;
import org.apache.geronimo.gshell.registry.AliasRegistry;
import org.apache.geronimo.gshell.vfs.provider.meta.data.MetaData;
import org.apache.geronimo.gshell.vfs.provider.meta.data.MetaDataRegistry;
import org.apache.geronimo.gshell.vfs.provider.meta.data.support.MetaDataRegistryConfigurer;

/**
 * Handles mapping of aliases under <tt>meta:/aliases</tt>.
 *
 * @version $Rev$ $Date$
 */
public class AliasMetaMapper
    implements EventListener
{
    private final EventManager eventManager;

    private final MetaDataRegistry metaRegistry;

    private final AliasRegistry aliasRegistry;

    private MetaDataRegistryConfigurer metaConfig;

    public AliasMetaMapper(final EventManager eventManager, final MetaDataRegistry metaRegistry, final AliasRegistry aliasRegistry) {
        assert eventManager != null;
        this.eventManager = eventManager;
        assert metaRegistry != null;
        this.metaRegistry = metaRegistry;
        assert aliasRegistry != null;
        this.aliasRegistry = aliasRegistry;
    }

    // @PostConstruct
    public synchronized void init() throws Exception {
        assert metaRegistry != null;
        metaConfig = new MetaDataRegistryConfigurer(metaRegistry);

        assert eventManager != null;
        eventManager.addListener(this);

        // Add existing aliases in case some have already been registered
        for (String name : aliasRegistry.getAliasNames()) {
            MetaData data = metaConfig.addFile("/aliases/" + name);
            data.addAttribute("ALIAS", aliasRegistry.getAlias(name));
        }
    }

    public synchronized void onEvent(final Event event) throws Exception {
        assert event != null;

        if (event instanceof AliasRegisteredEvent) {
            AliasRegisteredEvent targetEvent = (AliasRegisteredEvent)event;

            MetaData data = metaConfig.addFile("/aliases/" + targetEvent.getName());
            data.addAttribute("ALIAS", targetEvent.getAlias());
        }
        else if (event instanceof AliasRemovedEvent) {
            AliasRemovedEvent targetEvent = (AliasRemovedEvent)event;

            // TODO: Remove meta:/aliases/<name>
        }
    }
}