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

import org.apache.geronimo.gshell.application.Application;
import org.apache.geronimo.gshell.application.ApplicationManager;
import org.apache.geronimo.gshell.application.plugin.Plugin;
import org.apache.geronimo.gshell.application.plugin.PluginManager;
import org.apache.geronimo.gshell.artifact.ArtifactManager;
import org.apache.geronimo.gshell.event.Event;
import org.apache.geronimo.gshell.event.EventListener;
import org.apache.geronimo.gshell.event.EventManager;
import org.apache.geronimo.gshell.event.EventPublisher;
import org.apache.geronimo.gshell.model.application.PluginArtifact;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.spring.BeanContainerAware;
import org.apache.geronimo.gshell.wisdom.application.ApplicationConfiguredEvent;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.File;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of the {@link PluginManager} component.
 *
 * @version $Rev$ $Date$
 */
public class PluginManagerImpl
    implements PluginManager, BeanContainerAware
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationManager applicationManager;

    @Autowired
    private ArtifactManager artifactManager;

    @Autowired
    private EventManager eventManager;

    @Autowired
    private EventPublisher eventPublisher;

    private BeanContainer container;

    private Set<Plugin> plugins = new LinkedHashSet<Plugin>();

    public void setBeanContainer(final BeanContainer container) {
        assert container != null;
        
        this.container = container;
    }

    @PostConstruct
    public void init() {
        assert eventManager != null;
        eventManager.addListener(new EventListener() {
            public void onEvent(Event event) throws Exception {
                assert event != null;

                if (event instanceof ApplicationConfiguredEvent) {
                    ApplicationConfiguredEvent targetEvent = (ApplicationConfiguredEvent)event;

                    loadPlugins(targetEvent.getApplication());
                }
            }
        });
    }

    public Set<Plugin> getPlugins() {
        return plugins;
    }

    private void loadPlugins(final Application application) {
        assert application != null;

        log.debug("Loading plugins for application: {}", application.getId());

        List<PluginArtifact> artifacts = application.getModel().getPlugins(true);

        for (PluginArtifact artifact : artifacts) {
            try {
                loadPlugin(application, artifact);
            }
            catch (Exception e) {
                log.error("Failed to load plugin: " + artifact, e);
            }
        }
    }

    private void loadPlugin(final Application application, final PluginArtifact artifact) throws Exception {
        assert application != null;
        assert artifact != null;

        log.debug("Loading plugin: {}", artifact.getId());

        Set<Artifact> artifacts = resolveArtifacts(application, artifact);
        List<URL> classPath = createClassPath(artifacts);

        BeanContainer pluginContainer = container.createChild("gshell.plugin(" + artifact.getId() + ")", classPath);
        pluginContainer.start();
        
        log.debug("Created plugin container: {}", pluginContainer);
        
        PluginImpl plugin = pluginContainer.getBean(PluginImpl.class);

        // Initialize the plugins artifact configuration
        plugin.initArtifact(artifact);
        plugin.initArtifacts(artifacts);

        plugins.add(plugin);

        log.debug("Activating plugin: {}", plugin.getName());

        plugin.activate();

        eventPublisher.publish(new PluginLoadedEvent(plugin, artifact));
    }

    public void loadPlugin(final PluginArtifact artifact) throws Exception {
        assert applicationManager != null;
        loadPlugin(applicationManager.getApplication(), artifact);
    }

    private Set<Artifact> resolveArtifacts(final Application application, final PluginArtifact artifact) throws Exception {
        assert application != null;
        assert artifact != null;

        log.debug("Resolving plugin artifacts");
        
        ArtifactResolutionRequest request = new ArtifactResolutionRequest();
        request.setFilter(new PluginArtifactFilter(application));

        Set<Artifact> artifacts = new LinkedHashSet<Artifact>();
        assert artifactManager != null;
        ArtifactFactory factory = artifactManager.getArtifactFactory();

        Artifact pluginArtifact = factory.createArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), /*scope*/null, artifact.getType());
        assert pluginArtifact != null;

        log.debug("Plugin artifact: {}", pluginArtifact);

        artifacts.add(pluginArtifact);

        request.setArtifactDependencies(artifacts);

        ArtifactResolutionResult result = artifactManager.resolve(request);

        return result.getArtifacts();
    }

    private List<URL> createClassPath(final Set<Artifact> artifacts) throws Exception {
        assert artifacts != null;

        List<URL> classPath = new LinkedList<URL>();

        if (!artifacts.isEmpty()) {
            log.debug("Plugin classpath:");

            for (Artifact a : artifacts) {
                File file = a.getFile();
                assert file != null;

                URL url = file.toURI().toURL();
                log.debug("    {}", url);

                classPath.add(url);
            }
        }

        return classPath;
    }
}