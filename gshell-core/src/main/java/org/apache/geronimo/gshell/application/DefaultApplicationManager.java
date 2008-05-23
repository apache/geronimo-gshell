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

import org.apache.geronimo.gshell.GShell;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.artifact.ArtifactManager;
import org.apache.geronimo.gshell.lookup.EnvironmentLookup;
import org.apache.geronimo.gshell.lookup.IOLookup;
import org.apache.geronimo.gshell.model.application.Application;
import org.apache.geronimo.gshell.model.common.Dependency;
import org.apache.geronimo.gshell.model.common.RemoteRepository;
import org.apache.geronimo.gshell.model.common.LocalRepository;
import org.apache.geronimo.gshell.plexus.GShellPlexusContainer;
import org.apache.geronimo.gshell.shell.Environment;
import org.apache.geronimo.gshell.shell.InteractiveShell;
import org.apache.geronimo.gshell.shell.ShellInfo;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
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
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
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
@Component(role=ApplicationManager.class)
public class DefaultApplicationManager
    implements ApplicationManager, Contextualizable
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private ArtifactManager artifactManager;
    
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

        log.debug("Configuring; config: {}", config);

        // Validate the configuration
        config.validate();

        Application application = config.getApplication();
        log.debug("Application: {}", application);

        // Apply artifact manager configuration settings for application
        configureArtifactManager(application);

        // Create the application container
        container = createContainer(application);

        // TODO: Configure other application bits (branding, etc) ?
        // TODO: May want to have those components pull from the application's context instead, like we are doing for layout

        // Install lookup intestances
        IOLookup.set(container, config.getIo());
        EnvironmentLookup.set(container, config.getEnvironment());

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

    private void configureArtifactManager(final Application application) throws Exception {
        assert application != null;
        assert artifactManager != null;

        // Setup the local repository
        LocalRepository localRepository = application.getLocalRepository();

        if (localRepository != null) {
            artifactManager.setLocalRepository(localRepository.getDirectoryFile());
        }

        // Setup remote repositories
        List<RemoteRepository> remoteRepositories = application.remoteRepositories();

        if (remoteRepositories != null) {
            for (RemoteRepository repo : remoteRepositories) {
                artifactManager.addRemoteRepository(repo.getId(), repo.getLocationUri());
            }
        }
    }

    private GShellPlexusContainer createContainer(final Application application) throws Exception {
        assert application != null;

        log.debug("Creating application container");

        List<URL> classPath = createClassPath(application);
        ClassWorld classWorld = parentContainer.getContainerRealm().getWorld();
        ClassRealm realm = parentContainer.getContainerRealm().createChildRealm(application.getId());

        for (URL url : classPath) {
            realm.addURL(url);
        }

        ContainerConfiguration config = new DefaultContainerConfiguration();
        config.setName(application.getId());
        config.setClassWorld(classWorld);
        config.setRealm(realm);

        // HACK: Should need these here, but for some reason components are getting instantiated in the wrong container
        //       so for now to get something working again, use the parent containers configuration set in GShellBuilder

        // For now use the old Command* bits to get things working, then refactor to use the new Plugin* bits
        // config.addComponentDiscoverer(new CommandDiscoverer());
        // config.addComponentDiscoveryListener(new CommandCollector());
        // config.addComponentDiscoverer(new PluginDiscoverer());
        // config.addComponentDiscoveryListener(new PluginCollector());

        GShellPlexusContainer child = parentContainer.createChild(config);

        log.debug("Application container: {}", child);

        return child;
    }

    private List<URL> createClassPath(final Application application) throws MalformedURLException {
        assert application != null;

        ArtifactFactory factory = artifactManager.getArtifactFactory();

        Artifact originating = factory.createArtifact("dummy", "dummy", "dummy", null, "jar");

        ArtifactResolutionRequest request = new ArtifactResolutionRequest();
        request.setArtifact(originating);
        request.setFilter(new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME));

        Set<Artifact> artifacts = new LinkedHashSet<Artifact>();
        List<Dependency> dependencies = application.dependencies();

        if (dependencies != null && !dependencies.isEmpty()) {
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

        //
        // FIXME: Validate the result and complain if not valid (exceptions/missing/whatever), may want to move that to the AM
        //

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

    public GShell createShell() throws Exception {
        // Make sure that we have a valid context
        getContext();

        final InteractiveShell shell = container.lookupComponent(InteractiveShell.class);

        log.debug("Created shell instance: {}", shell);

        //
        // FIXME: Use a real proxy, so we can add generic interception muck to handle security muck and whatever ?
        //        or shall we use an aspect to handle this muck?
        //
        
        GShell proxy = new GShell() {
            public void run(final Object... args) throws Exception {
                shell.run(args);
            }

            public ShellInfo getShellInfo() {
                return shell.getShellInfo();
            }

            public Environment getEnvironment() {
                return shell.getEnvironment();
            }

            public Object execute(String line) throws Exception {
                return shell.execute(line);
            }

            public Object execute(String command, Object[] args) throws Exception {
                return shell.execute(command, args);
            }

            public Object execute(Object... args) throws Exception {
                return shell.execute(args);
            }

            public Object execute(Object[][] commands) throws Exception {
                return shell.execute(commands);
            }
        };

        log.debug("Create shell proxy: {}", proxy);

        return proxy;
    }
}