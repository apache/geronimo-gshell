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

package org.apache.geronimo.gshell.commands.repository;

import org.apache.geronimo.gshell.artifact.ArtifactManager;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Set;

/**
 * Resolve repository artifacts.
 *
 * @version $Rev$ $Date$
 */
public class ResolveAction
    implements CommandAction
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ArtifactManager artifactManager;

    //
    // TODO: Consider using <g>:<a>:<v>:<s>:<t> notation instead of, or in addtion this?
    //
    
    @Option(name="-g", aliases={"--groupId"}, argumentRequired=true, required=true)
    private String groupId;

    @Option(name="-a", aliases={"--artifactId"}, argumentRequired=true, required=true)
    private String artifactId;

    @Option(name="-v", aliases={"--version"}, argumentRequired=true, required=true)
    private String version;

    @Option(name="-t", aliases={"--type"}, argumentRequired=true)
    private String type = "jar";

    @Option(name="-c", aliases={"--classifier"}, argumentRequired=true)
    private String classifier;

    @Option(name="-s", aliases={"--scope"}, argumentRequired=true)
    private String scope;

    @Option(name="-T", aliases={"--transitive"})
    private boolean transitive;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;

        assert artifactManager != null;
        ArtifactFactory factory = artifactManager.getArtifactFactory();

        log.debug("Using factory: {}", factory);
        Artifact artifact;

        if (classifier != null) {
            artifact = factory.createArtifactWithClassifier(groupId, artifactId, version, type, classifier);
            artifact.setScope(scope);
        }
        else {
            artifact = factory.createArtifact(groupId, artifactId, version, scope, type);
        }

        log.debug("Created artifact: {}", artifact);

        ArtifactResolutionRequest request = new ArtifactResolutionRequest();

        IO io = context.getIo();

        //
        // TODO: Update the AM API to use this as originating when artifact == null and artifact dependencies != null
        //

        if (transitive) {
            io.info("Resolving artifact (transitively): {}", artifact);

            Artifact originating = factory.createArtifact("dummy", "dummy", "dummy", null, "jar");
            request.setArtifact(originating);
            request.setArtifactDependencies(Collections.singleton(artifact));
        }
        else {
            io.info("Resolving artifact: {}", artifact);

            request.setArtifact(artifact);
            Set<Artifact> deps = Collections.emptySet();
            request.setArtifactDependencies(deps);
        }

        if (scope != null) {
            io.debug("Using scope: {}", scope);
            
            request.setFilter(new ScopeArtifactFilter(scope));
        }

        ArtifactResolutionResult result = artifactManager.resolve(request);

        //
        // TODO: Do something with the result?
        //

        return Result.SUCCESS;
    }
}