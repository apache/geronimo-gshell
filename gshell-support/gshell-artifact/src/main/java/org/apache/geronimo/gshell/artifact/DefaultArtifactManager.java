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

package org.apache.geronimo.gshell.artifact;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.wagon.events.TransferListener;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the {@link ArtifactManager} component.
 *
 * @version $Rev$ $Date$
 */
@Component(role=ArtifactManager.class)
public class DefaultArtifactManager
    implements ArtifactManager
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private ArtifactFactory artifactFactory;

    @Requirement
    private ArtifactRepositoryFactory repositoryFactory;

    @Requirement
    private ArtifactResolver artifactResolver;

    @Requirement
    private ArtifactMetadataSource artifactMetadataSource;

    @Requirement
    private WagonManager wagonManager;

    private ArtifactRepository localRepository;

    private List<ArtifactRepository> remoteRepositories = new ArrayList<ArtifactRepository>();

    public ArtifactRepository getLocalRepository() {
        return localRepository;
    }

    public void setLocalRepository(final ArtifactRepository repository) throws InvalidRepositoryException {
        assert repository != null;

        localRepository = repository;

        log.debug("Using local repository: {}", repository);
    }

    public void setLocalRepository(final File dir) throws InvalidRepositoryException {
        assert dir != null;

        try {
            ArtifactRepository repo = repositoryFactory.createLocalRepository(dir);
            setLocalRepository(repo);
        }
        catch (Exception e) {
            throw new InvalidRepositoryException(e);
        }
    }

    public List<ArtifactRepository> getRemoteRepositories() {
        return remoteRepositories;
    }

    public void addRemoteRepository(final ArtifactRepository repository) throws InvalidRepositoryException {
        assert repository != null;

        remoteRepositories.add(repository);

        log.debug("Added remote repository: {}", repository);
    }

    public void addRemoteRepository(final String id, final URI location) throws InvalidRepositoryException {
        assert id != null;
        assert location != null;

        try {
            ArtifactRepository repo = repositoryFactory.createArtifactRepository(
                id,
                location.toURL().toExternalForm(),
                ArtifactRepositoryFactory.DEFAULT_LAYOUT_ID,

                // FIXME: Expose more configuration to user API
                new ArtifactRepositoryPolicy(),  // snapshots
                new ArtifactRepositoryPolicy()); // releases
            
            addRemoteRepository(repo);
        }
        catch (Exception e) {
            throw new InvalidRepositoryException(e);
        }
    }

    public ArtifactFactory getArtifactFactory() {
        return artifactFactory;
    }

    public void setDownloadMonitor(final TransferListener listener) {
        assert listener != null;

        wagonManager.setDownloadMonitor(listener);

        log.debug("Using download monitor: {}", listener);
    }
    
    public ArtifactResolutionResult resolve(final ArtifactResolutionRequest request) throws ArtifactResolutionException {
        assert request != null;

        // Automatically fill in some missing bits

        if (request.getLocalRepository() == null) {
            request.setLocalRepository(localRepository);
        }

        if (request.getRemoteRepostories() == null) {
            request.setRemoteRepostories(remoteRepositories);
        }

        if (request.getMetadataSource() == null) {
            request.setMetadataSource(artifactMetadataSource);
        }

        log.debug("Resolving request: {}", request);

        ArtifactResolutionResult result = artifactResolver.resolve(request);

        return validateResolutionResult(request, result);
    }

    private ArtifactResolutionResult validateResolutionResult(final ArtifactResolutionRequest request, final ArtifactResolutionResult result) throws ArtifactResolutionException {
        assert request != null;
        assert result != null;

        log.debug("Validating result: {}", result);

        if (/* TODO: detect failure */ false) {
            throw new ArtifactResolutionException(request, result);
        }

        return result;
    }
}