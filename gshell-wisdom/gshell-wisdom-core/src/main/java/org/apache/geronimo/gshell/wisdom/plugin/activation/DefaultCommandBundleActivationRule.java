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

package org.apache.geronimo.gshell.wisdom.plugin.activation;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.registry.CommandRegistry;
import org.apache.geronimo.gshell.registry.AliasRegistry;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.spring.BeanContainerAware;
import org.apache.geronimo.gshell.wisdom.plugin.bundle.CommandBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class DefaultCommandBundleActivationRule
    implements ActivationRule, BeanContainerAware
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private CommandRegistry commandRegistry;

    @Autowired
    private AliasRegistry aliasRegistry;

    private BeanContainer container;

    private String bundleName;

    public String getBundleName() {
        return bundleName;
    }

    public void setBundleName(final String bundleName) {
        assert bundleName != null;
        
        this.bundleName = bundleName;
    }

    public void setBeanContainer(final BeanContainer container) {
        assert container != null;

        this.container = container;
    }

    private CommandBundle getBundle() {
        assert container != null;
        Map<String, CommandBundle> bundles = container.getBeans(CommandBundle.class);

        assert bundleName != null;
        CommandBundle bundle = null;
        for (CommandBundle b : bundles.values()) {
            if (b.getName().equals(bundleName)) {
                bundle = b;
                break;
            }
        }

        if (bundle == null) {
            throw new RuntimeException("No bundle found with name: " + bundleName);
        }

        return bundle;
    }

    public void evaluate(final ActivationContext context) throws Exception {
        assert context != null;

        if (context.getTasks().isEmpty()) {
            ActivationTask task = new ActivationTask() {
                public void execute() throws Exception {
                    CommandBundle bundle = getBundle();
                    
                    log.debug("Processing command bundle: {}", bundle);

                    Map<String,Command> commands = bundle.getCommands();

                    for (String name : commands.keySet()) {
                        commandRegistry.registerCommand(name, commands.get(name));
                    }

                    Map<String,String> aliases = bundle.getAliases();

                    for (String name : aliases.keySet()) {
                        aliasRegistry.registerAlias(name, aliases.get(name));
                    }
                }
            };

            context.addTask(task);
        }
    }
}