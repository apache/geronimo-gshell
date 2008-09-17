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
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandRegistry;
import org.apache.geronimo.gshell.application.plugin.Plugin;
import org.apache.geronimo.gshell.wisdom.plugin.activation.ActivationRule;
import org.apache.geronimo.gshell.wisdom.plugin.activation.ActivationContext;
import org.apache.geronimo.gshell.wisdom.plugin.activation.ActivationTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Default implementation of {@link Plugin}.
 *
 * @version $Rev$ $Date$
 */
public class PluginImpl
    implements Plugin
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private String id;

    private List<ActivationRule> activationRules;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        assert id != null;
        
        this.id = id;
    }

    public List<ActivationRule> getActivationRules() {
        return activationRules;
    }

    public void setActivationRules(final List<ActivationRule> activationRules) {
        assert activationRules != null;

        this.activationRules = activationRules;
    }

    public void activate() {
        if (activationRules == null) {
            log.warn("No activation rules configured");
            return;
        }

        ActivationContext context = new ActivationContext() {
            private List<ActivationTask> tasks = new ArrayList<ActivationTask>();

            public List<ActivationTask> getTasks() {
                return tasks;
            }

            public void addTask(final ActivationTask task) {
                tasks.add(task);
            }
        };

        log.debug("Evaluating activation rules");

        for (ActivationRule rule : activationRules) {
            log.debug("Evaluating activation rule: {}", rule);

            try {
                rule.evaluate(context);
            }
            catch (Exception e) {
                log.warn("Failed to evaluate activation rule: " + rule, e);
            }
        }

        List<ActivationTask> tasks = context.getTasks();
        if (tasks.isEmpty()) {
            log.debug("No activation tasks configured in context");
            return;
        }

        log.debug("Executing activation tasks");
        
        for (ActivationTask task : tasks) {
            log.debug("Executing activation task: {}", task);
            
            try {
                task.execute();
            }
            catch (Exception e) {
                log.warn("Failed to execute activation task: " + task, e);
            }
        }
    }
}