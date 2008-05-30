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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.metadata.ResolutionGroup;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Custom {@link ArtifactMetadataSource} for GShell.
 *
 * @version $Rev$ $Date$
 */
@Component(role=ArtifactMetadataSource.class, hint="gshell")
public class GShellArtifactMetadataSource
    implements ArtifactMetadataSource
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Requirement(hint="maven")
    private ArtifactMetadataSource delegate;

    @Requirement
    private ArtifactRepositoryManager repositoryManager;

    public ResolutionGroup retrieve(final Artifact artifact, final ArtifactRepository localRepository, final List<ArtifactRepository> remoteRepositories)
        throws ArtifactMetadataRetrievalException
    {
        assert delegate != null;
        assert repositoryManager != null;

        log.trace("Retrieving metadata; artifact={}, local={}, remote={}", new Object[] { artifact, localRepository, remoteRepositories });

        ResolutionGroup result = delegate.retrieve(artifact, localRepository, repositoryManager.selectRemoteRepositories(remoteRepositories));

        log.trace("Resolution group; pom={}, artifacts={}, repositories={}", new Object[] { result.getPomArtifact(), result.getArtifacts(), result.getResolutionRepositories() });

        return new ResolutionGroup(result.getPomArtifact(), result.getArtifacts(), repositoryManager.selectRemoteRepositories(result.getResolutionRepositories()));
    }

    public List<ArtifactVersion> retrieveAvailableVersions(final Artifact artifact, final ArtifactRepository localRepository, final List<ArtifactRepository> remoteRepositories)
        throws ArtifactMetadataRetrievalException
    {
        assert delegate != null;
        assert repositoryManager != null;

        log.trace("Retrieving available versions; artifact={}, local={}, remote={}", new Object[] { artifact, localRepository, remoteRepositories });

        List<ArtifactVersion> versions = delegate.retrieveAvailableVersions(artifact, localRepository, repositoryManager.selectRemoteRepositories(remoteRepositories));

        log.trace("Available versions: {}", versions);
        
        return versions;
    }
}
