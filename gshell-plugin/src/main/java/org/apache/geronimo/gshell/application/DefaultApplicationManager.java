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

package org.apache.geronimo.gshell.application;

import org.apache.geronimo.gshell.artifact.ArtifactManager;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.model.application.Application;
import org.apache.geronimo.gshell.model.common.Dependency;
import org.apache.geronimo.gshell.model.common.LocalRepository;
import org.apache.geronimo.gshell.model.common.RemoteRepository;
import org.apache.geronimo.gshell.model.interpolate.Interpolator;
import org.apache.geronimo.gshell.model.interpolate.InterpolatorSupport;
import org.apache.geronimo.gshell.model.settings.Settings;
import org.apache.geronimo.gshell.plexus.GShellPlexusContainer;
import org.apache.geronimo.gshell.plugin.PluginCollector;
import org.apache.geronimo.gshell.plugin.PluginDiscoverer;
import org.apache.geronimo.gshell.settings.SettingsManager;
import org.apache.geronimo.gshell.shell.Environment;
import org.apache.geronimo.gshell.shell.Shell;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.filter.ExclusionSetFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.interpolation.PropertiesBasedValueSource;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of the {@link ApplicationManager} component.
 *
 * @version $Rev$ $Date$
 */
@Component(role=ApplicationManager.class)
public class DefaultApplicationManager
    implements ApplicationManager, Contextualizable
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private ArtifactManager artifactManager;

    @Requirement
    private SettingsManager settingsManager;

    private GShellPlexusContainer parentContainer;

    private GShellPlexusContainer container;

    private ApplicationContext applicationContext;

    public void contextualize(final Context context) throws ContextException {
        assert context != null;

        parentContainer = (GShellPlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
        
        log.debug("Parent container: {}", parentContainer);
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

            public Environment getEnvironment() {
                return config.getEnvironment();
            }

            public Application getApplication() {
                return config.getApplication();
            }
        };
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
        container = createContainer(application);
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

    private GShellPlexusContainer createContainer(final Application application) throws Exception {
        assert application != null;

        log.debug("Creating application container");

        List<URL> classPath = createClassPath(application);

        ClassWorld world = new ClassWorld();
        ClassRealm realm = world.newRealm("gshell.application[" + application.getId().replace(".", "/") + "]");
        realm.setParentRealm(parentContainer.getContainerRealm());

        for (URL url : classPath) {
            realm.addURL(url);
        }

        ContainerConfiguration config = new DefaultContainerConfiguration();
        config.setName(application.getId());
        config.setClassWorld(world);
        config.setRealm(realm);
        config.addComponentDiscoverer(new PluginDiscoverer());
        config.addComponentDiscoveryListener(new PluginCollector());

        GShellPlexusContainer child = parentContainer.createChild(config);

        log.debug("Application container: {}", child);

        return child;
    }

    private List<URL> createClassPath(final Application application) throws Exception {
        assert application != null;

        ArtifactResolutionRequest request = new ArtifactResolutionRequest();
        request.setFilter(new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME));

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

        //
        // FIXME: Load this list from build-generated properties or something like that
        //
        
        Set<String> excludes = new HashSet<String>();
        excludes.add("aspectjrt");
        excludes.add("plexus-classworlds");
        excludes.add("gshell-ansi");
        excludes.add("gshell-artifact");
        excludes.add("gshell-cli");
        excludes.add("gshell-clp");
        excludes.add("gshell-api");
        excludes.add("gshell-common");
        excludes.add("gshell-diet-log4j");
        excludes.add("gshell-i18n");
        excludes.add("gshell-io");
        excludes.add("gshell-plugin");
        excludes.add("gshell-model");
        excludes.add("gshell-plexus");
        excludes.add("jcl104-over-slf4j");
        excludes.add("jline");
        excludes.add("maven-artifact");
        excludes.add("maven-model");
        excludes.add("maven-profile");
        excludes.add("maven-project");
        excludes.add("maven-workspace");
        excludes.add("plexus-component-annotations");
        excludes.add("plexus-container-default");
        excludes.add("plexus-interpolation");
        excludes.add("plexus-utils");
        excludes.add("slf4j-api");
        excludes.add("slf4j-log4j12");
        excludes.add("wagon-file");
        excludes.add("wagon-http-lightweight");
        excludes.add("wagon-http-shared");
        excludes.add("wagon-provider-api");
        excludes.add("xpp3_min");
        excludes.add("xstream");

        ExclusionSetFilter filter = new ExclusionSetFilter(excludes);

        if (resolvedArtifacts != null && !resolvedArtifacts.isEmpty()) {
            log.debug("Application classpath:");

            for (Artifact artifact : resolvedArtifacts) {
                if (filter.include(artifact)) {
                    File file = artifact.getFile();
                    assert file != null;

                    URL url = file.toURI().toURL();
                    log.debug(" + {}", url);

                    classPath.add(url);
                }
            }
        }

        return classPath;
    }

    public Shell createShell() throws Exception {
        // Make sure that we have a valid context
        getContext();

        final Shell shell = container.lookupComponent(Shell.class);

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