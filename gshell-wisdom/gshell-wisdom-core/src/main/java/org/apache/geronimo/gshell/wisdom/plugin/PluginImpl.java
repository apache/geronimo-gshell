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

import org.apache.geronimo.gshell.application.plugin.Plugin;
import org.apache.geronimo.gshell.application.ClassPath;
import org.apache.geronimo.gshell.model.Artifact;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.spring.BeanContainerAware;
import org.apache.geronimo.gshell.wisdom.plugin.activation.ActivationContext;
import org.apache.geronimo.gshell.wisdom.plugin.activation.ActivationRule;
import org.apache.geronimo.gshell.wisdom.plugin.activation.ActivationTask;
import org.apache.geronimo.gshell.wisdom.plugin.bundle.Bundle;
import org.apache.geronimo.gshell.wisdom.plugin.bundle.CommandBundle;
import org.apache.geronimo.gshell.wisdom.plugin.bundle.NoSuchBundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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

    /*
    private boolean enabled = false;
    */
    
    private Artifact artifact;

    private ClassPath classPath;

    private List<String> bundleNames;

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
    
    public List<String> getBundleNames() {
        List<String> list = bundleNames;

        if (bundleNames == null) {
            list = Collections.emptyList();
        }

        return Collections.unmodifiableList(list);
    }

    public void setBundleNames(final List<String> bundleNames) {
        assert bundleNames != null;
        this.bundleNames = bundleNames;
    }

    public Bundle getBundle(final String name) throws NoSuchBundleException {
        assert container != null;
        Map<String, CommandBundle> bundles = container.getBeans(CommandBundle.class);

        CommandBundle bundle = null;
        for (CommandBundle b : bundles.values()) {
            if (b.getName().equals(name)) {
                bundle = b;
                break;
            }
        }

        if (bundle == null) {
            throw new NoSuchBundleException(name);
        }

        return bundle;
    }

    /*
    public synchronized boolean isEnabled() {
        return enabled;
    }

    public synchronized void enable() throws Exception {
        if (enabled) {
            throw new IllegalStateException("Plugin already enabled: " + name);
        }

        log.debug("Enabling plugin: {}", name);

        // TODO:

        enabled = true;
    }

    public synchronized void disable() throws Exception {
        if (!enabled) {
            throw new IllegalStateException("Plugin not enabled: " + name);
        }

        log.debug("Disabling bundle: {}", name);

        // TODO:

        enabled = false;
    }
    */

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