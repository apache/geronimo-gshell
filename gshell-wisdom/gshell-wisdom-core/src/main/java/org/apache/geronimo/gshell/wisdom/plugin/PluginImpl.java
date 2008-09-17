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

import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.spring.BeanContainerAware;
import org.apache.geronimo.gshell.command.CommandContainer;
import org.apache.geronimo.gshell.command.CommandContainerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class PluginImpl
    implements Plugin, BeanContainerAware
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private CommandContainerRegistry commandRegistry;

    private BeanContainer container;

    private String id;

    public void setBeanContainer(final BeanContainer container) {
        assert container != null;

        this.container = container;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        assert id != null;
        
        this.id = id;
    }

    public void activate() {
        log.debug("Activating");

        //
        // TODO: Create activate rules and execute them here.
        //
        
        assert container != null;
        Map<String, CommandBundle> bundles = container.getBeans(CommandBundle.class);

        log.debug("Discovered {} command bundles", bundles.size());

        for (CommandBundle bundle : bundles.values()) {
            log.debug("Processing command bundle: {}", bundle.getId());

            if (!bundle.isEmpty()) {
                log.debug("Discovered {} commands in bundle {}", bundle.size(), bundle.getId());

                for (CommandContainer command : bundle.getCommands()) {
                    commandRegistry.register(command);
                }
            }
        }
    }
}