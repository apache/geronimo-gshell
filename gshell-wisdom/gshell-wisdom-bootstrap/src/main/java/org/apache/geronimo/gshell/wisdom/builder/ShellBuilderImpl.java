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

package org.apache.geronimo.gshell.wisdom.builder;

import org.apache.geronimo.gshell.application.ApplicationConfiguration;
import org.apache.geronimo.gshell.application.ApplicationManager;
import org.apache.geronimo.gshell.artifact.ArtifactManager;
import org.apache.geronimo.gshell.artifact.monitor.ProgressSpinnerMonitor;
import org.apache.geronimo.gshell.chronos.StopWatch;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.io.SystemOutputHijacker;
import org.apache.geronimo.gshell.model.application.ApplicationModel;
import org.apache.geronimo.gshell.shell.Shell;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.spring.BeanContainerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GShell Wisdom("Spring") {@link ShellBuilder}.
 * 
 * @version $Rev$ $Date$
 */
public class ShellBuilderImpl
    implements ShellBuilder
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private BeanContainer container;

    private ClassLoader classLoader;

    private ApplicationManager applicationManager;

    private ApplicationConfiguration applicationConfig = new ApplicationConfiguration();

    private BeanContainer createContainer() throws Exception {
        BeanContainerImpl container = new BeanContainerImpl(getClassLoader());
        container.loadBeans(new String[] {
            "classpath*:META-INF/spring/components.xml"
        });

        return container;
    }

    private BeanContainer getContainer() {
        if (container == null) {
            try {
                container = createContainer();
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to create container", e);
            }
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

    public ApplicationModel getApplicationModel() {
        return applicationConfig.getModel();
    }

    public void setApplicationModel(final ApplicationModel applicationModel) {
        applicationConfig.setModel(applicationModel);
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

    //
    // ShellFactory
    //

    public Shell create() throws Exception {
        log.debug("Building");

        StopWatch watch = new StopWatch(true);
        
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
        BeanContainer container = getContainer();
        log.debug("Container: {}", container);

        //
        // TODO: Allow someway to configure a non-interactive monitor
        //

        // Configure download monitor
        ArtifactManager artifactManager = getContainer().getBean(ArtifactManager.class);
        artifactManager.setDownloadMonitor(new ProgressSpinnerMonitor(getIo()));

        // Configure application
        getApplicationManager().configure(applicationConfig);

        Shell shell = getApplicationManager().create();

        log.debug("Created shell in {}", watch);

        return shell;
    }
}