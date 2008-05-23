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
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.command.annotation.Requirement;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;

import java.util.Collections;

/**
 * Resolve repository artifacts.
 *
 * @version $Rev$ $Date$
 */
@CommandComponent(id="gshell-repository:resolve", description="Resolve repository artifacts")
public class ResolveCommand
    extends CommandSupport
{
    @Requirement
    private ArtifactManager artifactManager;

    //
    // TODO: Consider using <g>:<a>:<v>:<s>:<t> notation instead of this?
    //
    
    @Option(name="-g", aliases={"--groupId"}, argumentRequired=true, metaVar="GROUP-ID", required=true, description="Specify the artifact's groupId")
    private String groupId;

    @Option(name="-a", aliases={"--artifactId"}, argumentRequired=true, metaVar="ARTIFACT-ID", required=true, description="Specify the artifact's artifactId")
    private String artifactId;

    @Option(name="-v", aliases={"--version"}, argumentRequired=true, metaVar="VERSION", required=true, description="Specify the artifact's version")
    private String version;

    @Option(name="-t", aliases={"--type"}, argumentRequired=true, metaVar="TYPE", description="Specify the artifact's type")
    private String type = "jar";

    @Option(name="-s", aliases={"--scope"}, argumentRequired=true, metaVar="SCOPE", description="Specify the resolution scope")
    private String scope;

    protected Object doExecute() throws Exception {
        assert artifactManager != null;

        ArtifactFactory factory = artifactManager.getArtifactFactory();

        ArtifactResolutionRequest request = new ArtifactResolutionRequest();

        Artifact originating = factory.createArtifact("dummy", "dummy", "dummy", null, "jar");
        request.setArtifact(originating);

        if (scope != null) {
            request.setFilter(new ScopeArtifactFilter(scope));
        }

        Artifact artifact = factory.createArtifact(groupId, artifactId, version, scope, type);

        io.info("Resolving artifact: {}", artifact);
        
        request.setArtifactDependencies(Collections.singleton(artifact));

        ArtifactResolutionResult result = artifactManager.resolve(request);

        //
        // TODO: Do something with the result?
        //

        return SUCCESS;
    }
}