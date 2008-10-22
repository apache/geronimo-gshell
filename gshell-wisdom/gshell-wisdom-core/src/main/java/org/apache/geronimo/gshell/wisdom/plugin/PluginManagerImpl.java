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
import org.apache.geronimo.gshell.application.ClassPath;
import org.apache.geronimo.gshell.application.model.Artifact;
import org.apache.geronimo.gshell.application.plugin.Plugin;
import org.apache.geronimo.gshell.application.plugin.PluginManager;
import org.apache.geronimo.gshell.chronos.StopWatch;
import org.apache.geronimo.gshell.event.Event;
import org.apache.geronimo.gshell.event.EventListener;
import org.apache.geronimo.gshell.event.EventManager;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.spring.BeanContainerAware;
import org.apache.geronimo.gshell.wisdom.application.ApplicationConfiguredEvent;
import org.apache.geronimo.gshell.wisdom.application.ClassPathCache;
import org.apache.geronimo.gshell.wisdom.application.ClassPathImpl;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashSet;
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

    private final ApplicationManager applicationManager;

    private final EventManager eventManager;

    private BeanContainer container;

    private Set<Plugin> plugins = new LinkedHashSet<Plugin>();

    public PluginManagerImpl(final ApplicationManager applicationManager, final EventManager eventManager) {
        assert applicationManager != null;
        this.applicationManager = applicationManager;

        assert eventManager != null;
        this.eventManager = eventManager;
    }

    public void setBeanContainer(final BeanContainer container) {
        assert container != null;
        
        this.container = container;
    }

    // @PostConstruct
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

        List<Artifact> artifacts = application.getModel().getPlugins();

        for (Artifact artifact : artifacts) {
            try {
                loadPlugin(application, artifact);
            }
            catch (Exception e) {
                log.error("Failed to load plugin: " + artifact, e);
            }
        }
    }

    private void loadPlugin(final Application application, final Artifact artifact) throws Exception {
        assert application != null;
        assert artifact != null;

        StopWatch watch = new StopWatch(true);

        log.debug("Loading plugin: {}", artifact.getId());

        ClassPath classPath = loadClassPath(application, artifact);
        
        BeanContainer pluginContainer = container.createChild(classPath.getUrls());
        log.debug("Created plugin container: {}", pluginContainer);

        pluginContainer.loadBeans(new String[] {
            "classpath*:META-INF/gshell/components.xml"
        });
    
        PluginImpl plugin = pluginContainer.getBean(PluginImpl.class);

        // Initialize the plugins artifact configuration
        plugin.initArtifact(artifact);
        plugin.initClassPath(classPath);

        plugins.add(plugin);

        log.debug("Activating plugin: {}", plugin.getName());

        plugin.activate();

        log.debug("Loaded plugin in: {}", watch);
        
        eventManager.getPublisher().publish(new PluginLoadedEvent(plugin, artifact));
    }

    private ClassPath loadClassPath(final Application application, final Artifact artifact) throws Exception {
        assert application != null;
        assert artifact != null;

        // FIXME: Get basedir from application
        ClassPathCache cache = new ClassPathCache(new File(new File(System.getProperty("gshell.home")), "var/" + artifact.getGroupId() + "/" + artifact.getArtifactId() + "/classpath.ser"));
        ClassPath classPath = cache.get();

        if (classPath == null) {
            Set<Artifact> artifacts = resolveArtifacts(application, artifact);
            classPath = new ClassPathImpl(artifacts);
            cache.set(classPath);
        }

        if (log.isDebugEnabled()) {
            log.debug("Plugin classpath:");

            for (URL url : classPath.getUrls()) {
                log.debug("    {}", url);
            }
        }

        return classPath;
    }

    public void loadPlugin(final Artifact artifact) throws Exception {
        assert applicationManager != null;
        loadPlugin(applicationManager.getApplication(), artifact);
    }

    private Set<Artifact> resolveArtifacts(final Application application, final Artifact artifact) throws Exception {
        assert application != null;
        assert artifact != null;

        log.debug("Resolving plugin artifacts");

        Set<Artifact> artifacts = new LinkedHashSet<Artifact>();

        ResolveOptions options = new ResolveOptions();
        options.setOutputReport(true);
        options.setTransitive(true);
        options.setArtifactFilter(new PluginArtifactFilter(application));

        ModuleDescriptor md = createPluginModuleDescriptor(artifact);

        StopWatch watch = new StopWatch(true);

        Ivy ivy = container.getBean("ivy", Ivy.class);
        
        ResolveReport resolveReport = ivy.resolve(md, options);

        log.debug("Resolve completed in: {}", watch);

        if (resolveReport.hasError()) {
            log.error("Report has errors:");
            // noinspection unchecked
            List<String> problems = resolveReport.getAllProblemMessages();
            for (String problem : problems) {
                log.error("    {}", problem);
            }
        }

        log.debug("Plugin artifacts:");
        for (ArtifactDownloadReport downloadReport : resolveReport.getAllArtifactsReports()) {
            org.apache.ivy.core.module.descriptor.Artifact downloadedArtifact = downloadReport.getArtifact();
            ModuleRevisionId id = downloadedArtifact.getModuleRevisionId();

            Artifact resolved = new Artifact();
            resolved.setGroupId(id.getOrganisation());
            resolved.setArtifactId(id.getName());
            resolved.setVersion(id.getRevision());
            resolved.setType(downloadedArtifact.getType());
            resolved.setFile(downloadReport.getLocalFile());
            artifacts.add(resolved);
            
            log.debug("    {}", resolved.getId());
        }

        return artifacts;
    }

    private ModuleDescriptor createPluginModuleDescriptor(final Artifact artifact) {
        assert artifact != null;

        ModuleRevisionId pluginId = ModuleRevisionId.newInstance("gshell.plugin-" + artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
        DefaultModuleDescriptor md = new DefaultModuleDescriptor(pluginId, "integration", null, true);
        md.addConfiguration(new Configuration("default"));
        md.setLastModified(System.currentTimeMillis());

        ModuleRevisionId depId = ModuleRevisionId.newInstance(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
        DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md, depId, /* force */ false, /* changing*/ false, /* transitive */ true);
        dd.addDependencyConfiguration("default", "default");
        md.addDependency(dd);

        return md;
    }
}