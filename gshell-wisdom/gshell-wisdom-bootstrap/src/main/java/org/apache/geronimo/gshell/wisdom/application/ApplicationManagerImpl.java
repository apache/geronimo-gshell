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
import org.apache.geronimo.gshell.application.plugin.PluginManager;
import org.apache.geronimo.gshell.artifact.ArtifactManager;
import org.apache.geronimo.gshell.chronos.StopWatch;
import org.apache.geronimo.gshell.event.EventPublisher;
import org.apache.geronimo.gshell.model.application.ApplicationModel;
import org.apache.geronimo.gshell.model.application.DependencyArtifact;
import org.apache.geronimo.gshell.model.common.LocalRepository;
import org.apache.geronimo.gshell.model.common.RemoteRepository;
import org.apache.geronimo.gshell.shell.Shell;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.spring.BeanContainerAware;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Autowired
    private ArtifactManager artifactManager;

    @Autowired
    private EventPublisher eventPublisher;
    
    private BeanContainer container;

    private BeanContainer applicationContainer;

    private Application application;
    
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

        // Apply artifact manager configuration settings for application
        configureArtifactManager(config.getModel());

        application = loadApplication(config);

        log.debug("Application configured");

        // HACK: Force the plugin manager to boot up before we fire the event
        applicationContainer.getBean(PluginManager.class);
        
        eventPublisher.publish(new ApplicationConfiguredEvent(application));
    }

    private void configureArtifactManager(final ApplicationModel model) throws Exception {
        assert model != null;
        assert artifactManager != null;

        // Setup the local repository
        LocalRepository localRepository = model.getLocalRepository();

        if (localRepository != null) {
            artifactManager.getRepositoryManager().setLocalRepository(localRepository.getDirectoryFile());
        }

        // Setup remote repositories
        for (RemoteRepository repo : model.getRemoteRepositories()) {
            artifactManager.getRepositoryManager().addRemoteRepository(repo.getId(), repo.getLocationUri());
        }
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

        BeanContainer child = container.createChild("gshell.application(" + model.getId() + ")", classPath.getUrls());
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

        Set<Artifact> artifacts = resolveArtifacts(model);
        ClassPath classPath = new ClassPathImpl(artifacts);

        /*
        FIXME: This needs to find a way to work w/o XStream, which isn't available on the classpath yet.

        File file = new File(new File(System.getProperty("gshell.home")), "var/xstore/classpath.xml");  // FIXME: Get state directory from application/branding
        ClassPath classPath;

        if (file.exists()) {
            XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(file)));
            classPath = (ClassPath)decoder.readObject();
            log.debug("Loaded classpath from cache: {}", file);
            decoder.close();
        }
        else {
            Set<Artifact> artifacts = resolveArtifacts(model);
            classPath = new ClassPathImpl(artifacts);
            log.debug("Saving classpath to cache: {}", file);
            file.getParentFile().mkdirs();
            XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(file)));
            encoder.writeObject(classPath);
            encoder.close();
        }
        */

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

        ArtifactResolutionRequest request = new ArtifactResolutionRequest();
        request.setFilter(new ApplicationArtifactFilter());

        Set<Artifact> artifacts = new LinkedHashSet<Artifact>();
        List<DependencyArtifact> dependencies = model.getDependencies(true); // include groups

        if (!dependencies.isEmpty()) {
            assert artifactManager != null;
            ArtifactFactory factory = artifactManager.getArtifactFactory();

            log.debug("Application dependencies:");

            for (DependencyArtifact dep : dependencies) {
                Artifact artifact = factory.createArtifact(dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), /*scope*/null, dep.getType());
                assert artifact != null;

                log.debug("    {}", artifact);

                artifacts.add(artifact);
            }
        }

        request.setArtifactDependencies(artifacts);

        ArtifactResolutionResult result = artifactManager.resolve(request);

        return result.getArtifacts();
    }

    //
    // ShellFactory
    //

    public Shell create() throws Exception {
        // Make sure that we have a valid context
        getApplication();

        final Shell shell = applicationContainer.getBean(Shell.class);

        log.debug("Created shell instance: {}", shell);

        InvocationHandler handler = new InvocationHandler()
        {
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

                //
                // TODO: This would be a good place to inject the shell or the shell context into a thread holder
                //
                
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