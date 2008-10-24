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

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.geronimo.gshell.application.plugin.Plugin;
import org.apache.geronimo.gshell.event.Event;
import org.apache.geronimo.gshell.event.EventListener;
import org.apache.geronimo.gshell.event.EventManager;
import org.apache.geronimo.gshell.vfs.provider.meta.MetaFileNameParser;
import org.apache.geronimo.gshell.vfs.provider.meta.data.MetaData;
import org.apache.geronimo.gshell.vfs.provider.meta.data.MetaDataRegistry;

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

    private final MetaFileNameParser nameParser = new MetaFileNameParser();

    public PluginMetaMapper(final EventManager eventManager, final MetaDataRegistry metaRegistry) {
        assert eventManager != null;
        this.eventManager = eventManager;

        assert metaRegistry != null;
        this.metaRegistry = metaRegistry;
    }
    
    // @PostConstruct
    public synchronized void init() {
        // TODO: add any existing plugins which may have been configured before we loaded
        
        eventManager.addListener(this);
    }

    public synchronized void onEvent(final Event event) throws Exception {
        assert event != null;

        if (event instanceof PluginLoadedEvent) {
            PluginLoadedEvent targetEvent = (PluginLoadedEvent)event;
            add(targetEvent.getPlugin());
        }

        // TODO: Handle removing meta:/plugins/<name>
    }

    private FileName createName(final String name) throws FileSystemException {
        assert name != null;
        return nameParser.parseUri("/plugins/" + name);
    }

    private void add(final Plugin plugin) throws Exception {
        assert plugin != null;

        FileName fileName = createName(plugin.getName());
        MetaData data = new MetaData(fileName, FileType.FILE);
        data.addAttribute("PLUGIN", plugin);

        metaRegistry.registerData(fileName, data);
    }

    private void remove(final String name) throws Exception {
        assert name != null;

        FileName fileName = createName(name);

        metaRegistry.removeData(fileName);
    }
}