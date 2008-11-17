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

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.geronimo.gshell.event.Event;
import org.apache.geronimo.gshell.event.EventListener;
import org.apache.geronimo.gshell.event.EventManager;
import org.apache.geronimo.gshell.registry.AliasRegistry;
import org.apache.geronimo.gshell.registry.CommandResolver;
import org.apache.geronimo.gshell.vfs.provider.meta.MetaFileNameParser;
import org.apache.geronimo.gshell.vfs.provider.meta.data.MetaData;
import org.apache.geronimo.gshell.vfs.provider.meta.data.MetaDataRegistry;

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

    private final MetaFileNameParser nameParser = new MetaFileNameParser();

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
        // Add existing aliases in case some have already been registered
        for (String name : aliasRegistry.getAliasNames()) {
            add(name, aliasRegistry.getAlias(name));
        }

        eventManager.addListener(this);
    }

    public synchronized void onEvent(final Event event) throws Exception {
        assert event != null;

        if (event instanceof AliasRegisteredEvent) {
            AliasRegisteredEvent targetEvent = (AliasRegisteredEvent)event;
            add(targetEvent.getName(), targetEvent.getAlias());
        }
        else if (event instanceof AliasRemovedEvent) {
            AliasRemovedEvent targetEvent = (AliasRemovedEvent)event;
            remove(targetEvent.getName());
        }
    }

    private FileName createName(final String name) throws FileSystemException {
        assert name != null;
        return nameParser.parseUri(CommandResolver.ALIASES_ROOT + "/" + name);
    }

    private void add(final String name, final String alias) throws Exception {
        assert name != null;
        assert alias != null;

        FileName fileName = createName(name);
        MetaData data = new MetaData(fileName, FileType.FILE);
        data.addAttribute("ALIAS", alias);

        metaRegistry.registerData(fileName, data);
    }

    //
    // FIXME: This seems to have problems, when adding/removing alias, first alias value keeps, probably due to the cached VFS FileObject
    //
    
    private void remove(final String name) throws Exception {
        assert name != null;

        FileName fileName = createName(name);

        metaRegistry.removeData(fileName);
    }
}