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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.geronimo.gshell.command.CommandRegistry;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.spring.BeanContainerAware;
import org.apache.geronimo.gshell.wisdom.plugin.CommandBundle;

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

    private BeanContainer container;

    private String bundleId;

    public void setBeanContainer(final BeanContainer container) {
        assert container != null;

        this.container = container;
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(final String bundleId) {
        this.bundleId = bundleId;
    }

    public void evaluate(final ActivationContext context) throws Exception {
        assert context != null;

        if (context.getTasks().isEmpty()) {
            ActivationTask task = new ActivationTask() {
                public void execute() throws Exception {
                    assert container != null;
                    Map<String, CommandBundle> bundles = container.getBeans(CommandBundle.class);

                    assert bundleId != null;
                    CommandBundle bundle = null;
                    for (CommandBundle b : bundles.values()) {
                        if (b.getId().equals(bundleId)) {
                            bundle = b;
                            break;
                        }
                    }

                    if (bundle == null) {
                        throw new Exception("No bundle found with id: " + bundleId);
                    }

                    log.debug("Processing command bundle: {}", bundle.getId());

                    if (!bundle.isEmpty()) {
                        log.debug("Discovered {} commands in bundle {}", bundle.size(), bundle.getId());

                        for (Command command : bundle.getCommands()) {
                            commandRegistry.register(command);
                        }
                    }
                }
            };

            context.addTask(task);
        }
    }
}