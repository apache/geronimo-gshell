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
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExclusionSetFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
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

    @SuppressWarnings({"UnusedDeclaration"})
    @PostConstruct
    private void init() {
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
                loadPlugin(artifact);
            }
            catch (Exception e) {
                log.error("Failed to load plugin: " + artifact, e);
            }
        }
    }

    public void loadPlugin(final PluginArtifact artifact) throws Exception {
        assert artifact != null;

        log.debug("Loading plugin: {}", artifact.getId());

        List<URL> classPath = createClassPath(artifact);

        BeanContainer pluginContainer = container.createChild("gshell.plugin[" + artifact.getId() + "]", classPath);
        pluginContainer.start();
        
        log.debug("Created plugin container: {}", pluginContainer);
        
        Plugin plugin = pluginContainer.getBean(Plugin.class);

        plugins.add(plugin);

        log.debug("Activating plugin: {}", plugin.getId());

        plugin.activate();

        eventPublisher.publish(new PluginLoadedEvent(plugin, artifact));
    }

    private List<URL> createClassPath(final PluginArtifact artifact) throws Exception {
        assert artifact != null;

        ArtifactResolutionRequest request = new ArtifactResolutionRequest();

        AndArtifactFilter filter = new AndArtifactFilter();

        filter.add(new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME));

        filter.add(new ExclusionSetFilter(new String[] {
            "aopalliance",
            "geronimo-annotation_1.0_spec",
            "gshell-ansi",
            "gshell-api",
            "gshell-application",
            "gshell-clp",
            "gshell-i18n",
            "gshell-io",
            "gshell-model",
            "gshell-spring",
            "gshell-yarn",
            "gshell-interpolation",
            "gshell-layout",
            "jcl104-over-slf4j",
            "jline",
            "plexus-classworlds",
            "slf4j-api",
            "spring-core",
            "spring-context",
            "spring-beans",
            "xpp3_min",
            "xstream",
        }));

        request.setFilter(filter);

        Set<Artifact> artifacts = new LinkedHashSet<Artifact>();
        ArtifactFactory factory = artifactManager.getArtifactFactory();

        Artifact pluginArtifact = factory.createArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), /*scope*/null, artifact.getType());
        assert pluginArtifact != null;

        log.debug("Plugin artifact: {}", pluginArtifact);

        artifacts.add(pluginArtifact);

        request.setArtifactDependencies(artifacts);

        ArtifactResolutionResult result = artifactManager.resolve(request);

        List<URL> classPath = new LinkedList<URL>();
        Set<Artifact> resolvedArtifacts = result.getArtifacts();

        if (resolvedArtifacts != null && !resolvedArtifacts.isEmpty()) {
            log.debug("Plugin classpath:");

            for (Artifact a : resolvedArtifacts) {
                File file = a.getFile();
                assert file != null;

                URL url = file.toURI().toURL();
                log.debug(" + {}", url);

                classPath.add(url);
            }
        }

        return classPath;
    }
}