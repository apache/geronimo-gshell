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

package org.apache.geronimo.gshell.wisdom.application;

import org.apache.geronimo.gshell.application.Application;
import org.apache.geronimo.gshell.application.ApplicationConfiguration;
import org.apache.geronimo.gshell.application.ApplicationManager;
import org.apache.geronimo.gshell.application.ApplicationSecurityManager;
import org.apache.geronimo.gshell.application.ClassPath;
import org.apache.geronimo.gshell.application.model.ApplicationModel;
import org.apache.geronimo.gshell.application.model.Artifact;
import org.apache.geronimo.gshell.application.plugin.PluginManager;
import org.apache.geronimo.gshell.chronos.StopWatch;
import org.apache.geronimo.gshell.event.EventPublisher;
import org.apache.geronimo.gshell.shell.Shell;
import org.apache.geronimo.gshell.shell.ShellContext;
import org.apache.geronimo.gshell.shell.ShellContextHolder;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.spring.BeanContainerAware;
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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of the {@link ApplicationManager} component.
 *
 * @version $Rev$ $Date$
 */
public class ApplicationManagerImpl
    implements ApplicationManager, BeanContainerAware
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final EventPublisher eventPublisher;

    private BeanContainer container;

    private BeanContainer applicationContainer;

    private Application application;

    public ApplicationManagerImpl(final EventPublisher eventPublisher) {
        assert eventPublisher != null;
        this.eventPublisher = eventPublisher;
    }

    public void setBeanContainer(final BeanContainer container) {
        assert container != null;
        
        this.container = container;
    }

    public Application getApplication() {
        if (application == null) {
            throw new IllegalStateException("Application has not been configured");
        }

        return application;
    }

    public void configure(final ApplicationConfiguration config) throws Exception {
        assert config != null;

        log.trace("Configuring; config: {}", config);

        // Validate the configuration
        config.validate();

        application = loadApplication(config);

        log.debug("Application configured");

        // HACK: Force the plugin manager to boot up before we fire the event
        applicationContainer.getBean(PluginManager.class);
        
        eventPublisher.publish(new ApplicationConfiguredEvent(application));
    }

    private ApplicationImpl loadApplication(final ApplicationConfiguration config) throws Exception {
        assert config != null;

        StopWatch watch = new StopWatch(true);

        ApplicationImpl app = new ApplicationImpl(config);
        ApplicationModel model = app.getModel();

        log.debug("Loading application: {}", app.getId());
        log.trace("Application model: {}", model);

        ClassPath classPath = loadClassPath(model);
        app.initClassPath(classPath);

        BeanContainer child = container.createChild(classPath.getUrls());
        log.debug("Application container: {}", child);

        child.loadBeans(new String[] {
            "classpath*:META-INF/spring/components.xml"
        });
        
        applicationContainer = child;

        log.debug("Application loaded in: {}", watch);

        return app;
    }

    private ClassPath loadClassPath(final ApplicationModel model) throws Exception {
        assert model != null;

        // FIXME: Get basedir from application
        ClassPathCache cache = new ClassPathCache(new File(new File(System.getProperty("gshell.home")), "var/classpath.ser"));
        ClassPath classPath = cache.get();

        if (classPath == null) {
            Set<Artifact> artifacts = resolveArtifacts(model);
            classPath = new ClassPathImpl(artifacts);
            cache.set(classPath);
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Application classpath:");

            for (URL url : classPath.getUrls()) {
                log.debug("    {}", url);
            }
        }
        
        return classPath;
    }

    private Set<Artifact> resolveArtifacts(final ApplicationModel model) throws Exception {
        assert model != null;

        log.debug("Resolving application artifacts");

        Set<Artifact> artifacts = new LinkedHashSet<Artifact>();

        ResolveOptions options = new ResolveOptions();
        options.setOutputReport(true);
        options.setTransitive(true);
        options.setArtifactFilter(new ApplicationArtifactFilter());

        ModuleDescriptor md = createApplicationModuleDescriptor(model);

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

        log.debug("Application artifacts:");
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

    private ModuleDescriptor createApplicationModuleDescriptor(final ApplicationModel model) {
        assert model != null;

        ModuleRevisionId appId = ModuleRevisionId.newInstance("gshell.application-" + model.getGroupId(), model.getArtifactId(), model.getVersion());
        DefaultModuleDescriptor md = new DefaultModuleDescriptor(appId, "integration", null, true);
        md.addConfiguration(new Configuration("default"));
        md.setLastModified(System.currentTimeMillis());

        for (Artifact dep : model.getDependencies()) {
            ModuleRevisionId depId = ModuleRevisionId.newInstance(dep.getGroupId(), dep.getArtifactId(), dep.getVersion());
            DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md, depId, /* force */ false, /* changing*/ false, /* transitive */ true);
            dd.addDependencyConfiguration("default", "default");
            md.addDependency(dd);
        }

        return md;
    }

    //
    // ShellFactory
    //

    public Shell create() throws Exception {
        // Make sure that we have a valid context
        getApplication();

        final Shell shell = applicationContainer.getBean(Shell.class);

        final ShellContext context = shell.getContext();

        log.debug("Created shell instance: {}", shell);

        InvocationHandler handler = new InvocationHandler() {
            //
            // FIXME: Need to resolve how to handle the security manager for the application,
            //        the SM is not thread-specific, but VM specific... so not sure this is
            //        the right approache at all :-(
            //

            private final ApplicationSecurityManager sm = new ApplicationSecurityManager();

            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                assert proxy != null;
                assert method != null;
                // args may be null

                if (method.getDeclaringClass() == Object.class) {
                    return method.invoke(this, args);
                }

                final ShellContext prevContext = ShellContextHolder.get(true);
                ShellContextHolder.set(context);

                final SecurityManager prevSM = System.getSecurityManager();
                System.setSecurityManager(sm);
                
                try {
                    return method.invoke(shell, args);
                }
                catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }
                finally {
                    System.setSecurityManager(prevSM);
                    ShellContextHolder.set(prevContext);
                }
            }
        };

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Shell proxy = (Shell) Proxy.newProxyInstance(cl, new Class[] { Shell.class }, handler);

        log.debug("Create shell proxy: {}", proxy);

        eventPublisher.publish(new ShellCreatedEvent(proxy));
        
        return proxy;
    }
}