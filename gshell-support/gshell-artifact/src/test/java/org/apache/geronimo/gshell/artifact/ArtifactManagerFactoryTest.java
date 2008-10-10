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

import org.apache.geronimo.gshell.spring.SpringTestSupport;
import org.apache.geronimo.gshell.chronos.StopWatch;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.Artifact;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.LinkedHashSet;
import java.io.File;

/**
 * Unit tests for the {@link ArtifactManagerFactory} class.
 *
 * @version $Rev$ $Date$
 */
public class ArtifactManagerFactoryTest
    extends SpringTestSupport
{
    /*
    public void testProcessor() throws Exception {
        ArtifactManager artifactManager = getBeanContainer().getBean("artifactManager", ArtifactManager.class);
        assertNotNull(artifactManager);
    }
    */
    
    public void testResolve() throws Exception {
        ArtifactManager artifactManager = getBean(ArtifactManager.class);
        artifactManager.getRepositoryManager().setLocalRepository(new File("/tmp/repo"));
        artifactManager.getRepositoryManager().addRemoteRepository("local-cache", new File("/Users/jason/.m2/repository").toURI());
        
        ArtifactResolutionRequest request = new ArtifactResolutionRequest();
        Set<Artifact> deps = new LinkedHashSet<Artifact>();
        deps.add(artifactManager.getArtifactFactory().createArtifact("org.apache.geronimo.gshell.wisdom", "gshell-wisdom-bootstrap", "1.0-alpha-2-SNAPSHOT", Artifact.SCOPE_RUNTIME, "jar"));
        request.setArtifactDependencies(deps);

        StopWatch watch = new StopWatch(true);

        ArtifactResolutionResult result = artifactManager.resolve(request);
        log.debug("Resolution completed in: {}", watch);
    }
}