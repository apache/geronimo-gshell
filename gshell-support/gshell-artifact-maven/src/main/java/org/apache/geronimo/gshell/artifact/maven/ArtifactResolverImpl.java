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

import org.apache.geronimo.gshell.artifact.ArtifactResolver;
import org.apache.geronimo.gshell.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.LinkedHashSet;

/**
 * <a href="http://maven.apache.org">Apache Maven (maven-artifact)</a> based {@link ArtifactResolver}.
 *
 * @version $Rev$ $Date$
 */
public class ArtifactResolverImpl
    implements ArtifactResolver
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ArtifactManager artifactManager;

    public ArtifactResolverImpl(final ArtifactManager artifactManager) {
        assert artifactManager != null;
        this.artifactManager = artifactManager;
    }

    public Result resolve(final Request request) throws Failure {
        assert request != null;

        ArtifactResolutionRequest _request = new ArtifactResolutionRequest();

        if (request.artifact != null) {
            log.debug("Artifact: {}", request.artifact);

            _request.setArtifact(createArtifact(request.artifact));
        }

        if (request.artifacts != null) {
            Set<org.apache.maven.artifact.Artifact> artifacts = new LinkedHashSet<org.apache.maven.artifact.Artifact>();

            log.debug("Dependencies:");

            for (Artifact source : request.artifacts) {
                org.apache.maven.artifact.Artifact artifact = createArtifact(source);
                log.debug("    {}", artifact);
                artifacts.add(artifact);
            }

            _request.setArtifactDependencies(artifacts);
        }

        // Always filter for runtime scope
        AndArtifactFilter filter = new AndArtifactFilter();
        _request.setFilter(filter);
        filter.add(new ScopeArtifactFilter(org.apache.maven.artifact.Artifact.SCOPE_RUNTIME));

        if (request.filter != null) {
            log.debug("Filter: {}", request.filter);
            
            filter.add(new ArtifactFilter() {
                public boolean include(final org.apache.maven.artifact.Artifact source) {
                    assert source != null;
                    Artifact artifact = createArtifact(source);
                    return request.filter.accept(artifact);
                }
            });
        }

        Result result = new Result();
        try {
            ArtifactResolutionResult _result = artifactManager.resolve(_request);
            result.artifacts = new LinkedHashSet<Artifact>();

            log.debug("Resolved:");

            for (org.apache.maven.artifact.Artifact source : _result.getArtifacts()) {
                Artifact artifact = createArtifact(source);

                log.debug("    {}", artifact);
                
                result.artifacts.add(artifact);
            }
        }
        catch (ResolutionException e) {
            throw new Failure(e);
        }

        return result;
    }

    private Artifact createArtifact(final org.apache.maven.artifact.Artifact source) {
        assert source != null;

        Artifact artifact = new Artifact();
        artifact.setGroup(source.getGroupId());
        artifact.setName(source.getArtifactId());
        artifact.setVersion(source.getVersion());
        artifact.setClassifier(source.getClassifier());
        artifact.setType(source.getType());
        artifact.setFile(source.getFile());

        return artifact;
    }

    private org.apache.maven.artifact.Artifact createArtifact(final Artifact source) {
        assert source != null;

        ArtifactFactory factory = artifactManager.getArtifactFactory();

        org.apache.maven.artifact.Artifact artifact = factory.createArtifact(source.getGroup(), source.getName(), source.getVersion(), null, source.getType());

        artifact.setFile(source.getFile());
        
        return artifact;
    }
}