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

import org.apache.geronimo.gshell.application.ApplicationConfiguration;
import org.apache.geronimo.gshell.application.ApplicationContext;
import org.apache.geronimo.gshell.application.ApplicationManager;
import org.apache.geronimo.gshell.application.ApplicationSecurityManager;
import org.apache.geronimo.gshell.application.settings.SettingsManager;
import org.apache.geronimo.gshell.artifact.ArtifactManager;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.model.application.Application;
import org.apache.geronimo.gshell.model.common.Dependency;
import org.apache.geronimo.gshell.model.common.LocalRepository;
import org.apache.geronimo.gshell.model.common.RemoteRepository;
import org.apache.geronimo.gshell.model.interpolate.Interpolator;
import org.apache.geronimo.gshell.model.interpolate.InterpolatorSupport;
import org.apache.geronimo.gshell.model.settings.Settings;
import org.apache.geronimo.gshell.shell.Shell;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.spring.BeanContainerAware;
import org.apache.geronimo.gshell.wisdom.application.event.ApplicationConfiguredEvent;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.filter.ExclusionSetFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.codehaus.plexus.interpolation.PropertiesBasedValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.LinkedList;
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
    private SettingsManager settingsManager;

    private ApplicationContext applicationContext;

    private BeanContainer container;

    private BeanContainer applicationContainer;

    public void setBeanContainer(final BeanContainer container) {
        assert container != null;
        
        this.container = container;
    }

    public ApplicationContext getContext() {
        if (applicationContext == null) {
            throw new IllegalStateException("Application has not been configured");
        }

        return applicationContext;
    }

    public void configure(final ApplicationConfiguration config) throws Exception {
        assert config != null;

        log.trace("Configuring; config: {}", config);

        // Validate the configuration
        config.validate();

        // Interpolate the model
        interpolate(config);

        // Configure the application
        configure(config.getApplication());

        // Create a new context
        applicationContext = new ApplicationContext() {
            public IO getIo() {
                return config.getIo();
            }

            public Variables getVariables() {
                return config.getVariables();
            }

            public Application getApplication() {
                return config.getApplication();
            }
        };

        log.debug("Application configured");
        
        applicationContainer.publish(new ApplicationConfiguredEvent(this));
    }

    private void interpolate(final ApplicationConfiguration config) throws Exception {
    	assert config != null;

        Application app = config.getApplication();
        Interpolator<Application> interp = new InterpolatorSupport<Application>();

        // Add value sources to resolve muck
        interp.addValueSource(new PropertiesBasedValueSource(System.getProperties()));

        // User settings should override the applications
        assert settingsManager != null;
        Settings settings = settingsManager.getSettings();
        if (settings != null) {
            interp.addValueSource(new PropertiesBasedValueSource(settings.getProperties()));
        }

        // Add application settings
        interp.addValueSource(new PropertiesBasedValueSource(app.getProperties()));

        app = interp.interpolate(app);

        // Update the configuration with the new model
        config.setApplication(app);
    }

    private void configure(final Application application) throws Exception {
        assert application != null;

        log.debug("Application ID: {}", application.getId());
        log.trace("Application descriptor: {}", application);

        // Apply artifact manager configuration settings for application
        configureArtifactManager(application);

        // Create the application container
        applicationContainer = createContainer(application);
    }

    private void configureArtifactManager(final Application application) throws Exception {
        assert application != null;
        assert artifactManager != null;

        // Setup the local repository
        LocalRepository localRepository = application.getLocalRepository();

        if (localRepository != null) {
            artifactManager.getRepositoryManager().setLocalRepository(localRepository.getDirectoryFile());
        }

        // Setup remote repositories
        for (RemoteRepository repo : application.remoteRepositories()) {
            artifactManager.getRepositoryManager().addRemoteRepository(repo.getId(), repo.getLocationUri());
        }
    }

    private BeanContainer createContainer(final Application application) throws Exception {
        assert application != null;

        log.debug("Creating application container");

        List<URL> classPath = createClassPath(application);

        BeanContainer child = container.createChild(application.getId(), classPath);

        log.debug("Application container: {}", child);

        return child;
    }

    private List<URL> createClassPath(final Application application) throws Exception {
        assert application != null;

        ArtifactResolutionRequest request = new ArtifactResolutionRequest();

        AndArtifactFilter filter = new AndArtifactFilter();

        filter.add(new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME));

        filter.add(new ExclusionSetFilter(new String[] {
            //
            // FIXME: Load this list from build-generated properties or something like that
            //

            "aopalliance",
            "aspectjrt",
            "geronimo-annotation_1.0_spec",
            "gshell-ansi",
            "gshell-api",
            "gshell-artifact",
            "gshell-application",
            "gshell-cli",
            "gshell-clp",
            "gshell-i18n",
            "gshell-io",
            "gshell-model",
            "gshell-spring",
            "gshell-wisdom-bootstrap",
            "gshell-yarn",
            "jcl104-over-slf4j",
            "jline",
            "log4j",
            "maven-artifact",
            "maven-model",
            "maven-profile",
            "maven-project",
            "maven-workspace",
            "maven-settings",
            "maven-plugin-registry",
            "plexus-component-annotations",
            "plexus-container-default",
            "plexus-interpolation",
            "plexus-utils",
            "plexus-classworlds",
            "slf4j-api",
            "slf4j-log4j12",
            "spring-core",
            "spring-context",
            "spring-beans",
            "wagon-file",
            "wagon-http-lightweight",
            "wagon-http-shared",
            "wagon-provider-api",
            "xbean-reflect",
            "xpp3_min",
            "xstream",
        }));

        request.setFilter(filter);

        Set<Artifact> artifacts = new LinkedHashSet<Artifact>();
        List<Dependency> dependencies = application.dependencies(true); // include groups

        if (!dependencies.isEmpty()) {
            ArtifactFactory factory = artifactManager.getArtifactFactory();

            log.debug("Application dependencies:");

            for (Dependency dep : dependencies) {
                Artifact artifact = factory.createArtifact(dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), null, dep.getType());
                assert artifact != null;

                log.debug(" + {}", artifact);

                artifacts.add(artifact);
            }
        }

        request.setArtifactDependencies(artifacts);

        ArtifactResolutionResult result = artifactManager.resolve(request);

        List<URL> classPath = new LinkedList<URL>();
        Set<Artifact> resolvedArtifacts = result.getArtifacts();

        if (resolvedArtifacts != null && !resolvedArtifacts.isEmpty()) {
            log.debug("Application classpath:");

            for (Artifact artifact : resolvedArtifacts) {
                File file = artifact.getFile();
                assert file != null;

                URL url = file.toURI().toURL();
                log.debug(" + {}", url);

                classPath.add(url);
            }
        }

        return classPath;
    }

    public Shell create() throws Exception {
        // Make sure that we have a valid context
        getContext();

        // Have to use named instance to prevent unique lookup problems due to shell also being a CommandLineExecutor instance
        final Shell shell = applicationContainer.getBean("shell", Shell.class);

        log.debug("Created shell instance: {}", shell);

        InvocationHandler handler = new InvocationHandler()
        {
            //
            // FIXME: Need to resolve how to handle the security manager for the application,
            //        the SM is not thread-specific, but VM specific... so not sure this is
            //        the right approache at all :-(
            //

            private final ApplicationSecurityManager securityManager = new ApplicationSecurityManager();

            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                assert proxy != null;
                assert method != null;
                // args may be null

                if (method.getDeclaringClass() == Object.class) {
                    return method.invoke(this, args);
                }

                SecurityManager previous = System.getSecurityManager();
                System.setSecurityManager(securityManager);
                try {
                    return method.invoke(shell, args);
                }
                catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }
                finally {
                    System.setSecurityManager(previous);
                }
            }
        };

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Shell proxy = (Shell) Proxy.newProxyInstance(cl, new Class[] { Shell.class }, handler);

        log.debug("Create shell proxy: {}", proxy);

        return proxy;
    }
}