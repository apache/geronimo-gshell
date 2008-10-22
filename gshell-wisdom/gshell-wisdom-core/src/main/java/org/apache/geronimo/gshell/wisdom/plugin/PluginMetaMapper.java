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

package org.apache.geronimo.gshell.wisdom.plugin;

import org.apache.geronimo.gshell.event.Event;
import org.apache.geronimo.gshell.event.EventListener;
import org.apache.geronimo.gshell.event.EventManager;
import org.apache.geronimo.gshell.vfs.provider.meta.data.MetaData;
import org.apache.geronimo.gshell.vfs.provider.meta.data.MetaDataRegistry;
import org.apache.geronimo.gshell.vfs.provider.meta.data.support.MetaDataRegistryConfigurer;

/**
 * Handles mapping of plugins under <tt>meta:/plugins</tt>.
 *
 * @version $Rev$ $Date$
 */
public class PluginMetaMapper
    implements EventListener
{
    private final EventManager eventManager;

    private final MetaDataRegistry metaRegistry;

    private MetaDataRegistryConfigurer metaConfig;

    public PluginMetaMapper(final EventManager eventManager, final MetaDataRegistry metaRegistry) {
        assert eventManager != null;
        this.eventManager = eventManager;
        assert metaRegistry != null;
        this.metaRegistry = metaRegistry;
    }
    
    // @PostConstruct
    public void init() {
        assert metaRegistry != null;
        metaConfig = new MetaDataRegistryConfigurer(metaRegistry);

        assert eventManager != null;
        eventManager.addListener(this);
    }

    public void onEvent(final Event event) throws Exception {
        assert event != null;

        if (event instanceof PluginLoadedEvent) {
            PluginLoadedEvent targetEvent = (PluginLoadedEvent)event;

            MetaData data = metaConfig.addFile("/plugins/" + targetEvent.getPlugin().getName());
            data.addAttribute("PLUGIN", targetEvent.getPlugin());
        }

        // TODO: Handle removing meta:/plugins/<name>
    }
}