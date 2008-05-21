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
import org.apache.geronimo.gshell.artifact.ArtifactManager;
import org.apache.geronimo.gshell.lookup.EnvironmentLookup;
import org.apache.geronimo.gshell.lookup.IOLookup;
import org.apache.geronimo.gshell.model.application.Application;
import org.apache.geronimo.gshell.model.common.Dependency;
import org.apache.geronimo.gshell.model.common.SourceRepository;
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

    private ApplicationConfiguration applicationConfig;

    public void contextualize(final Context context) throws ContextException {
        assert context != null;

        parentContainer = (GShellPlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
        
        log.debug("Parent container: {}", parentContainer);
    }

    // createContext()
    
    // getContext()

    public void configure(final ApplicationConfiguration config) throws Exception {
        assert config != null;

        log.debug("Configuring; config: {}", config);

        // Validate the configuration
        Application application = config.getApplication();
        if (application == null) {
            throw new IllegalStateException("Missing application configuration");
        }
        log.debug("Application: {}", application);

        // Apply artifact manager configuration settings for application
        configureArtifactManager(application);

        // Create the application container
        container = createContainer(application);

        // TODO: Configure other application bits (branding, layout)

        // Install lookup intestances
        IOLookup.set(container, config.getIo());
        EnvironmentLookup.set(container, config.getEnvironment());

        // Track the configuration, mark configured
        applicationConfig = config;
    }

    private void configureArtifactManager(final Application application) throws Exception {
        assert application != null;
        assert artifactManager != null;

        // Setup the local repository
        File repository = application.getRepository();
        if (repository != null) {
            artifactManager.setLocalRepository(repository);
        }

        // Setup remote repositories
        List<SourceRepository> sourceRepositories = application.sourceRepositories();
        if (sourceRepositories != null) {
            for (SourceRepository repo : sourceRepositories) {
                String loc = repo.getLocation();
                URL url = new URL(loc);
                String id = url.getHost(); // FIXME: Need to expose the repo id in the model, for now assume the id is the hostname
                artifactManager.addRemoteRepository(id, url);
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

        ContainerConfiguration cc = new DefaultContainerConfiguration();
        cc.setName(application.getId());
        cc.setClassWorld(classWorld);
        cc.setRealm(realm);

        // HACK: Should need these here, but for some reason components are getting instantiated in the wrong container
        //       so for now to get something working again, use the parent containers configuration set in GShellBuilder

        // For now use the old Command* bits to get things working, then refactor to use the new Plugin* bits
        // cc.addComponentDiscoverer(new CommandDiscoverer());
        // cc.addComponentDiscoveryListener(new CommandCollector());
        // cc.addComponentDiscoverer(new PluginDiscoverer());
        // cc.addComponentDiscoveryListener(new PluginCollector());

        GShellPlexusContainer child = parentContainer.createChild(cc);

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
        if (applicationConfig == null) {
            throw new IllegalStateException("Not configured");
        }

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