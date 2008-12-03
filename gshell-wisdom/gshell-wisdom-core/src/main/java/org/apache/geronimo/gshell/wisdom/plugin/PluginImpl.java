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

import org.apache.geronimo.gshell.application.ClassPath;
import org.apache.geronimo.gshell.application.plugin.Plugin;
import org.apache.geronimo.gshell.application.plugin.activation.ActivationContext;
import org.apache.geronimo.gshell.application.plugin.activation.ActivationRule;
import org.apache.geronimo.gshell.application.plugin.activation.ActivationTask;
import org.apache.geronimo.gshell.application.plugin.bundle.Bundle;
import org.apache.geronimo.gshell.application.plugin.bundle.NoSuchBundleException;
import org.apache.geronimo.gshell.artifact.Artifact;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.spring.BeanContainerAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link Plugin}.
 *
 * @version $Rev$ $Date$
 */
public class PluginImpl
    implements Plugin, BeanContainerAware
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String name;

    private Artifact artifact;

    private ClassPath classPath;

    private Map<String,String> bundleIdMap;

    private List<ActivationRule> activationRules;

    private BeanContainer container;

    public PluginImpl(final String name) {
        assert name != null;

        this.name = name;
    }

    public void setBeanContainer(final BeanContainer container) {
        assert container != null;

        this.container = container;
    }

    public String getName() {
        return name;
    }

    public Artifact getArtifact() {
        if (artifact == null) {
            throw new IllegalStateException("Artifact not initialized");
        }
        return artifact;
    }

    void initArtifact(final Artifact artifact) {
        assert artifact != null;
        
        this.artifact = artifact;
    }

    public ClassPath getClassPath() {
        if (classPath == null) {
            throw new IllegalStateException("Classpath not initialized");
        }
        return classPath;
    }

    void initClassPath(final ClassPath classPath) {
        assert classPath != null;

        this.classPath = classPath;
    }
    
    public Collection<String> getBundleNames() {
        Collection<String> names;

        if (bundleIdMap == null) {
            names = Collections.emptyList();
        }
        else {
            names = bundleIdMap.keySet();
        }

        return Collections.unmodifiableCollection(names);
    }

    public void setBundleIdMap(final Map<String, String> bundleIdMap) {
        assert bundleIdMap != null;
        this.bundleIdMap = bundleIdMap;
    }

    public Bundle getBundle(final String name) throws NoSuchBundleException {
        assert name != null;
        assert bundleIdMap != null;
        String id = bundleIdMap.get(name);

        if (id == null) {
            throw new NoSuchBundleException(name);
        }

        assert container != null;
        return container.getBean(id, Bundle.class);
    }

    public List<ActivationRule> getActivationRules() {
        return activationRules;
    }

    public void setActivationRules(final List<ActivationRule> rules) {
        assert rules != null;

        this.activationRules = rules;
    }

    public void activate() {
        if (activationRules == null) {
            log.warn("No activation rules configured");
            return;
        }

        ActivationContext context = new ActivationContext() {
            private List<ActivationTask> tasks = new ArrayList<ActivationTask>();

            public Plugin getPlugin() {
                return PluginImpl.this;
            }

            public List<ActivationTask> getTasks() {
                return tasks;
            }

            public void addTask(final ActivationTask task) {
                tasks.add(task);
            }
        };

        log.trace("Evaluating activation rules");

        for (ActivationRule rule : activationRules) {
            log.trace("Evaluating activation rule: {}", rule);

            try {
                rule.evaluate(context);
            }
            catch (Exception e) {
                log.warn("Failed to evaluate activation rule: " + rule, e);
            }
        }

        List<ActivationTask> tasks = context.getTasks();
        if (tasks.isEmpty()) {
            log.warn("No activation tasks configured in context");
            return;
        }

        log.trace("Executing activation tasks");
        
        for (ActivationTask task : tasks) {
            log.trace("Executing activation task: {}", task);
            
            try {
                task.execute();
            }
            catch (Exception e) {
                log.warn("Failed to execute activation task: " + task, e);
            }
        }
    }
}