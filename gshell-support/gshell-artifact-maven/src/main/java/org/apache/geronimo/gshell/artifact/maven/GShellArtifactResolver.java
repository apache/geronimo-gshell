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

package org.apache.geronimo.gshell.artifact.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.ResolutionListener;
import org.apache.maven.artifact.resolver.conflict.ConflictResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Custom {@link ArtifactResolver} for GShell.
 *
 * @version $Rev$ $Date$
 */
@SuppressWarnings({"deprecation"})
@Component(role=ArtifactResolver.class, hint="gshell")
public class GShellArtifactResolver
    implements ArtifactResolver
{
    @Requirement(hint="delegate")
    private ArtifactResolver delegate;

    @Requirement
    private ArtifactRepositoryManager repositoryManager;

    @Deprecated
    public void resolve(final Artifact artifact, final List<ArtifactRepository> remoteRepositories, final ArtifactRepository localRepository) throws ArtifactResolutionException, ArtifactNotFoundException {
        assert delegate != null;
        assert repositoryManager != null;

        delegate.resolve(artifact, repositoryManager.selectRemoteRepositories(remoteRepositories), localRepository);
    }

    @Deprecated
    public ArtifactResolutionResult resolveTransitively(final Set<Artifact> artifacts, final Artifact artifact, final List<ArtifactRepository> remoteRepositories, final ArtifactRepository localRepository, final ArtifactMetadataSource source) throws ArtifactResolutionException, ArtifactNotFoundException {
        assert delegate != null;
        assert repositoryManager != null;
        
        return delegate.resolveTransitively(artifacts, artifact, repositoryManager.selectRemoteRepositories(remoteRepositories), localRepository, source);
    }

    @Deprecated
    public ArtifactResolutionResult resolveTransitively(final Set<Artifact> artifacts, final Artifact artifact, final List<ArtifactRepository> remoteRepositories, final ArtifactRepository localRepository, final ArtifactMetadataSource source, final List<ResolutionListener> listeners) throws ArtifactResolutionException, ArtifactNotFoundException {
        assert delegate != null;
        assert repositoryManager != null;
        
        return delegate.resolveTransitively(artifacts, artifact, repositoryManager.selectRemoteRepositories(remoteRepositories), localRepository, source, listeners);
    }

    @Deprecated
    public ArtifactResolutionResult resolveTransitively(final Set<Artifact> artifacts, final Artifact artifact, final ArtifactRepository localRepository, final List<ArtifactRepository> remoteRepositories, final ArtifactMetadataSource source, final ArtifactFilter filter) throws ArtifactResolutionException, ArtifactNotFoundException {
        assert delegate != null;
        assert repositoryManager != null;
        
        return delegate.resolveTransitively(artifacts, artifact, localRepository, repositoryManager.selectRemoteRepositories(remoteRepositories), source, filter);
    }

    @Deprecated
    public ArtifactResolutionResult resolveTransitively(final Set<Artifact> artifacts, final Artifact artifact, final Map managedVersions, final ArtifactRepository localRepository, final List<ArtifactRepository> remoteRepositories, final ArtifactMetadataSource source) throws ArtifactResolutionException, ArtifactNotFoundException {
        assert delegate != null;
        assert repositoryManager != null;
        
        return delegate.resolveTransitively(artifacts, artifact, managedVersions, localRepository, repositoryManager.selectRemoteRepositories(remoteRepositories), source);
    }

    @Deprecated
    public ArtifactResolutionResult resolveTransitively(final Set<Artifact> artifacts, final Artifact artifact, final Map managedVersions, final ArtifactRepository localRepository, final List<ArtifactRepository> remoteRepositories, final ArtifactMetadataSource source, final ArtifactFilter filter) throws ArtifactResolutionException, ArtifactNotFoundException {
        assert delegate != null;
        assert repositoryManager != null;
        
        return delegate.resolveTransitively(artifacts, artifact, managedVersions, localRepository, repositoryManager.selectRemoteRepositories(remoteRepositories), source, filter);
    }

    @Deprecated
    public ArtifactResolutionResult resolveTransitively(final Set<Artifact> artifacts, final Artifact artifact, final Map managedVersions, final ArtifactRepository localRepository, final List<ArtifactRepository> remoteRepositories, final ArtifactMetadataSource source, final ArtifactFilter filter, final List<ResolutionListener> listeners) throws ArtifactResolutionException, ArtifactNotFoundException {
        assert delegate != null;
        assert repositoryManager != null;
        
        return delegate.resolveTransitively(artifacts, artifact, managedVersions, localRepository, repositoryManager.selectRemoteRepositories(remoteRepositories), source, filter, listeners);
    }

    @Deprecated
    public ArtifactResolutionResult resolveTransitively(final Set<Artifact> artifacts, final Artifact artifact, final Map managedVersions, final ArtifactRepository localRepository, final List<ArtifactRepository> remoteRepositories, final ArtifactMetadataSource source, final ArtifactFilter filter, final List<ResolutionListener> listeners, final List<ConflictResolver> conflictResolvers) throws ArtifactResolutionException, ArtifactNotFoundException {
        assert delegate != null;
        assert repositoryManager != null;
        
        return delegate.resolveTransitively(artifacts, artifact, managedVersions, localRepository, repositoryManager.selectRemoteRepositories(remoteRepositories), source, filter, listeners, conflictResolvers);
    }

    @Deprecated
    public void resolveAlways(final Artifact artifact, final List<ArtifactRepository> remoteRepositories, final ArtifactRepository localRepository) throws ArtifactResolutionException, ArtifactNotFoundException {
        assert delegate != null;
        assert repositoryManager != null;
        
        delegate.resolveAlways(artifact, repositoryManager.selectRemoteRepositories(remoteRepositories), localRepository);
    }

    public ArtifactResolutionResult resolve(final ArtifactResolutionRequest request) {
        assert request != null;
        assert delegate != null;
        assert repositoryManager != null;
        
        request.setRemoteRepostories(repositoryManager.selectRemoteRepositories(request.getRemoteRepostories()));

        return delegate.resolve(request);
    }
}