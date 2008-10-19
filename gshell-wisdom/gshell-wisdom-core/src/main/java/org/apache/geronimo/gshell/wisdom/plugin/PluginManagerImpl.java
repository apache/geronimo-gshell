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
import org.apache.geronimo.gshell.event.EventPublisher;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.spring.BeanContainerAware;
import org.apache.geronimo.gshell.wisdom.application.ApplicationConfiguredEvent;
import org.apache.geronimo.gshell.wisdom.application.ClassPathImpl;
import org.apache.geronimo.gshell.xstore.XStore;
import org.apache.geronimo.gshell.xstore.XStoreRecord;
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

import javax.annotation.PostConstruct;
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

    private final EventPublisher eventPublisher;

    private final XStore xstore;

    private final Ivy ivy;

    private BeanContainer container;

    private Set<Plugin> plugins = new LinkedHashSet<Plugin>();

    public PluginManagerImpl(final ApplicationManager applicationManager, final EventManager eventManager, final EventPublisher eventPublisher, final XStore xstore, final Ivy ivy) {
        assert applicationManager != null;
        this.applicationManager = applicationManager;
        assert eventManager != null;
        this.eventManager = eventManager;
        assert eventPublisher != null;
        this.eventPublisher = eventPublisher;
        assert xstore != null;
        this.xstore = xstore;
        assert ivy != null;
        this.ivy = ivy;
    }

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
        
        BeanContainer pluginContainer = container.createChild("gshell.plugin(" + artifact.getId() + ")", classPath.getUrls());
        log.debug("Created plugin container: {}", pluginContainer);

        pluginContainer.loadBeans(new String[] {
            "classpath*:META-INF/spring/components.xml"
        });
    
        PluginImpl plugin = pluginContainer.getBean(PluginImpl.class);

        // Initialize the plugins artifact configuration
        plugin.initArtifact(artifact);
        plugin.initClassPath(classPath);

        plugins.add(plugin);

        log.debug("Activating plugin: {}", plugin.getName());

        plugin.activate();

        log.debug("Loaded plugin in: {}", watch);
        
        eventPublisher.publish(new PluginLoadedEvent(plugin, artifact));
    }

    private ClassPath loadClassPath(final Application application, final Artifact artifact) throws Exception {
        assert application != null;
        assert artifact != null;

        ClassPath classPath;
        // FIXME: Get state directory from application/branding
        XStoreRecord record = xstore.resolveRecord(artifact.getGroupId() + "/" + artifact.getArtifactId() + "/classpath.xml");
        if (record.exists()) {
            classPath = record.get(ClassPathImpl.class);
            log.debug("Loaded classpath from cache: {}", record);
        }
        else {
            Set<Artifact> artifacts = resolveArtifacts(application, artifact);
            classPath = new ClassPathImpl(artifacts);
            log.debug("Saving classpath to cache: {}", record);
            record.set(classPath);
        }
        record.close();

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