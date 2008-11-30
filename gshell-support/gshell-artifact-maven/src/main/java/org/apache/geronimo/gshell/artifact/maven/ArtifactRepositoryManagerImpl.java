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

import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the {@link ArtifactRepositoryManager} component.
 *
 * @version $Rev$ $Date$
 */
@Component(role=ArtifactRepositoryManager.class)
public class ArtifactRepositoryManagerImpl
    implements ArtifactRepositoryManager
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private ArtifactRepositoryFactory repositoryFactory;

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

        ArtifactRepository repo = repositoryFactory.createLocalRepository(dir);
        setLocalRepository(repo);
    }

    public List<ArtifactRepository> getRemoteRepositories() {
        return remoteRepositories;
    }

    public void addRemoteRepository(final ArtifactRepository repository) throws InvalidRepositoryException {
        assert repository != null;

        remoteRepositories.add(repository);

         log.debug("Added remote repository: {}", repository);
    }

    public List<ArtifactRepository> selectRemoteRepositories(final List<ArtifactRepository> repositories) {
        assert repositories != null;

        // For now just ignore the given repositories, eventually may want to apply some configurable allow/deny logic
        return getRemoteRepositories();
    }

    public void addRemoteRepository(final String id, final URI location) throws InvalidRepositoryException {
        assert id != null;
        assert location != null;

        try {
            ArtifactRepository repo = repositoryFactory.createArtifactRepository(
                id,
                location.toURL().toExternalForm(),
                ArtifactRepositoryFactory.DEFAULT_LAYOUT_ID,
                new ArtifactRepositoryPolicy(),  // snapshots
                new ArtifactRepositoryPolicy()); // releases

            addRemoteRepository(repo);
        }
        catch (MalformedURLException e) {
            throw new InvalidRepositoryException(e.getMessage(), id, e);
        }
    }
}