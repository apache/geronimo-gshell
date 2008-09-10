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

package org.apache.geronimo.gshell.wisdom;

import org.apache.geronimo.gshell.application.ApplicationConfiguration;
import org.apache.geronimo.gshell.application.ApplicationManager;
import org.apache.geronimo.gshell.application.settings.SettingsConfiguration;
import org.apache.geronimo.gshell.application.settings.SettingsManager;
import org.apache.geronimo.gshell.artifact.ArtifactManager;
import org.apache.geronimo.gshell.artifact.monitor.ProgressSpinnerMonitor;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.io.SystemOutputHijacker;
import org.apache.geronimo.gshell.model.application.Application;
import org.apache.geronimo.gshell.model.settings.Settings;
import org.apache.geronimo.gshell.shell.Shell;
import org.apache.geronimo.gshell.shell.ShellFactory;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.spring.BeanContainerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds {@link org.apache.geronimo.gshell.shell.Shell} instances.
 *
 * @version $Rev$ $Date$
 */
public class ShellBuilder
    implements ShellFactory
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private BeanContainer container;

    private ClassLoader classLoader;
    
    private SettingsManager settingsManager;

    private SettingsConfiguration settingsConfig = new SettingsConfiguration();

    private ApplicationManager applicationManager;

    private ApplicationConfiguration applicationConfig = new ApplicationConfiguration();

    private ArtifactManager artifactManager;

    public ShellBuilder() {}

    private BeanContainer createContainer() {
        return new BeanContainerImpl(getClassLoader());
    }

    private BeanContainer getContainer() {
        if (container == null) {
            container = createContainer();
        }
        return container;
    }
    
    public ClassLoader getClassLoader() {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        return classLoader;
    }

    public void setClassLoader(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public IO getIo() {
        return applicationConfig.getIo();
    }

    public void setIo(final IO io) {
        applicationConfig.setIo(io);
    }

    public Variables getVariables() {
        return applicationConfig.getVariables();
    }

    public void setVariables(final Variables variables) {
        applicationConfig.setVariables(variables);
    }

    public Settings getSettings() {
        return settingsConfig.getSettings();
    }

    public void setSettings(final Settings settings) {
        settingsConfig.setSettings(settings);
    }

    private SettingsManager createSettingsManager() {
        return getContainer().getBean(SettingsManager.class);
    }

    public SettingsManager getSettingsManager() {
        if (settingsManager == null) {
            settingsManager = createSettingsManager();
        }
        return settingsManager;
    }

    public void setSettingsManager(final SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    public Application getApplication() {
        return applicationConfig.getApplication();
    }

    public void setApplication(final Application application) {
        applicationConfig.setApplication(application);
    }

    private ApplicationManager createApplicationManager() {
        return getContainer().getBean(ApplicationManager.class);
    }

    public ApplicationManager getApplicationManager() {
        if (applicationManager == null) {
            applicationManager = createApplicationManager();
        }
        return applicationManager;
    }

    public void setApplicationManager(final ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

    private ArtifactManager createArtifactManager() {
        return getContainer().getBean(ArtifactManager.class);
    }

    public ArtifactManager getArtifactManager() {
        if (artifactManager == null) {
            artifactManager = createArtifactManager();
        }
        return artifactManager;
    }

    public void setArtifactManager(final ArtifactManager artifactManager) {
        this.artifactManager = artifactManager;
    }

    //
    // ShellFactory
    //

    public Shell create() throws Exception {
        log.debug("Building");

        // Set some defaults
        if (applicationConfig.getIo() == null) {
            applicationConfig.setIo(new IO());
        }
        if (applicationConfig.getVariables() == null) {
            applicationConfig.setVariables(new Variables());
        }

        // Hijack the system output streams
        if (!SystemOutputHijacker.isInstalled()) {
            SystemOutputHijacker.install();
        }

        // Register the application IO streams
        IO io = getIo();
        SystemOutputHijacker.register(io.outputStream, io.errorStream);

        // Initialize the container
        getContainer();
        log.debug("Container: {}", container);

        //
        // TODO: Allow someway to configure a non-interactive monitor
        //

        // Configure download monitor
        getArtifactManager().setDownloadMonitor(new ProgressSpinnerMonitor(getIo()));

        // Configure settings
        getSettingsManager().configure(settingsConfig);

        // Configure application
        getApplicationManager().configure(applicationConfig);

        return getApplicationManager().create();
    }
}