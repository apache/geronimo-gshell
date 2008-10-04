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

import org.apache.geronimo.gshell.event.EventPublisher;
import org.apache.geronimo.gshell.registry.AliasRegistry;
import org.apache.geronimo.gshell.registry.NoSuchAliasException;
import org.apache.geronimo.gshell.vfs.provider.meta.MetaFileDataRegistry;
import org.apache.geronimo.gshell.vfs.provider.meta.MetaFileDataRegistryConfigurer;
import org.apache.geronimo.gshell.vfs.provider.meta.MetaFileData;
import org.apache.geronimo.gshell.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link AliasRegistry} component.
 *
 * @version $Rev$ $Date$
 */
public class AliasRegistryImpl
    implements AliasRegistry
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private MetaFileDataRegistry metaRegistry;

    private MetaFileDataRegistryConfigurer metaConfig;

    private final Map<String,String> aliases = new LinkedHashMap<String,String>();

    @PostConstruct
    public void init() {
        assert metaRegistry != null;
        metaConfig = new MetaFileDataRegistryConfigurer(metaRegistry);
        metaConfig.addFolder("/aliases");
    }

    public void registerAlias(final String name, final String alias) {
        assert name != null;
        assert alias != null;

        log.debug("Registering alias: {} -> {}", name, alias);

        if (containsAlias(name)) {
            log.debug("Replacing alias: {}", name);
        }

        assert metaConfig != null;
        MetaFileData data = metaConfig.addFile("/aliases/" + name);
        data.addAttribute("ALIAS", alias);

        aliases.put(name, alias);

        eventPublisher.publish(new AliasRegisteredEvent(name, alias));
    }

    public void removeAlias(final String name) throws NoSuchAliasException {
        assert name != null;

        log.debug("Removing alias: {}", name);
        
        if (!containsAlias(name)) {
            throw new NoSuchAliasException(name);
        }

        // TODO: Remove from meta
        
        aliases.remove(name);

        eventPublisher.publish(new AliasRemovedEvent(name));
    }

    public String getAlias(final String name) throws NoSuchAliasException {
        assert name != null;

        if (!containsAlias(name)) {
            throw new NoSuchAliasException(name);
        }

        return aliases.get(name);
    }

    public boolean containsAlias(final String name) {
        assert name != null;

        return aliases.containsKey(name);
    }

    public Collection<String> getAliasNames() {
        return Collections.unmodifiableSet(aliases.keySet());
    }
}