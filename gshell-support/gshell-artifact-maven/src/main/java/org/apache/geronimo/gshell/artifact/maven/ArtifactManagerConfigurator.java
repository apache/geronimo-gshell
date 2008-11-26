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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.Map;

/**
 * Component to configure the {@link ArtifactManager}.
 *
 * @version $Rev$ $Date$
 */
public class ArtifactManagerConfigurator
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ArtifactManager artifactManager;

    private File localRepository;

    private Map<String, URI> remoteRepositories;

    public ArtifactManagerConfigurator(final ArtifactManager artifactManager) {
        assert artifactManager != null;
        this.artifactManager = artifactManager;
    }

    public void setLocalRepository(final File dir) {
        assert dir != null;
        this.localRepository = dir;
    }

    public void setRemoteRepositories(final Map<String,URI> repositories) {
        assert repositories != null;
        this.remoteRepositories = repositories;
    }

    // @PostConstruct
    public void init() throws Exception {
        ArtifactRepositoryManager repositoryManager = artifactManager.getRepositoryManager();
        log.debug("Configuring artifact manager: {}", artifactManager);
        
        if (localRepository != null) {
            repositoryManager.setLocalRepository(localRepository);
        }

        if (remoteRepositories != null) {
            for (Map.Entry<String,URI> entry : remoteRepositories.entrySet()) {
                repositoryManager.addRemoteRepository(entry.getKey(), entry.getValue());
            }
        }
    }
}