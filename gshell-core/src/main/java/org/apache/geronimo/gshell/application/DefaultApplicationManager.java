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
import org.apache.geronimo.gshell.lookup.EnvironmentLookup;
import org.apache.geronimo.gshell.lookup.IOLookup;
import org.apache.geronimo.gshell.model.application.Application;
import org.apache.geronimo.gshell.model.common.Dependency;
import org.apache.geronimo.gshell.model.common.SourceRepository;
import org.apache.geronimo.gshell.plexus.GShellPlexusContainer;
import org.apache.geronimo.gshell.plugin.CommandDiscoverer;
import org.apache.geronimo.gshell.plugin.CommandDiscoveryListener;
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
import java.net.URL;
import java.util.LinkedHashSet;
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

        Application application = config.getApplication();
        if (application == null) {
            throw new IllegalStateException("Missing application configuration");
        }

        // Setup artifact manager repositories
        assert artifactManager != null;
        
        File repository = application.getRepository();
        if (repository != null) {
            artifactManager.setLocalRepository(repository);
        }

        List<SourceRepository> sourceRepositories = application.sourceRepositories();
        if (sourceRepositories != null) {
            for (SourceRepository repo : sourceRepositories) {
                String loc = repo.getLocation();
                URL url = new URL(loc);
                String id = url.getHost(); // FIXME: Need to expose the repo id in the model, for now assume the id is the hostname
                artifactManager.addRemoteRepository(id, url);
            }
        }

        // Configure other application bits (branding, layout)
        // TODO:

        // Setup container
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
        // TODO: Validate the result and complain if not valid (exceptions/missing/whatever), may want to move that to the AM

        ClassWorld classWorld = parentContainer.getContainerRealm().getWorld();
        ClassRealm realm = parentContainer.getContainerRealm().createChildRealm(application.getId());
        Set<Artifact> resolvedArtifacts = result.getArtifacts();

        if (resolvedArtifacts != null && !resolvedArtifacts.isEmpty()) {
            log.debug("Application classpath:");

            for (Artifact artifact : resolvedArtifacts) {
                File file = artifact.getFile();
                assert file != null;

                log.debug(" + {}", file);

                realm.addURL(file.toURI().toURL());
            }
        }

        ContainerConfiguration cc = new DefaultContainerConfiguration();
        cc.setName(application.getId());
        cc.setClassWorld(classWorld);
        cc.setRealm(realm);

        // For now use the old Command* bits to get things working, then refactor to use the new Plugin* bits
        cc.addComponentDiscoverer(new CommandDiscoverer());
        cc.addComponentDiscoveryListener(new CommandDiscoveryListener());
        // cc.addComponentDiscoverer(new PluginDiscoverer());
        // cc.addComponentDiscoveryListener(new PluginCollector());

        container = parentContainer.createChild(cc);
        log.debug("Container: {}", container);

        // Install lookup intestances
        IOLookup.set(container, config.getIo());
        EnvironmentLookup.set(container, config.getEnvironment());
    }
}