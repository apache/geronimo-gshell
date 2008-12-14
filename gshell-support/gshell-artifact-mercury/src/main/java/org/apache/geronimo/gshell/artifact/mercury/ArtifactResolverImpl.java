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

package org.apache.geronimo.gshell.artifact.mercury;

import org.apache.geronimo.gshell.artifact.ArtifactResolver;
import org.apache.geronimo.gshell.artifact.Artifact;
import org.apache.geronimo.gshell.artifact.transfer.TransferListener;
import org.apache.maven.mercury.metadata.DependencyBuilderFactory;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <a href="http://maven.apache.org/mercury">Apache Maven Mercury</a>-based {@link ArtifactResolver}.
 *
 * @version $Rev$ $Date$
 */
public class ArtifactResolverImpl
    implements ArtifactResolver
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    public void setTransferListener(final TransferListener listener) {
        assert listener != null;

        // TODO: Figure out how to install into Mercury
    }

    public Result resolve(final Request request) throws Failure {
        assert request != null;

        // TODO: Setup MercuryDependenciesFilter
        
        DependencyBuilderFactory factory = new DependencyBuilderFactory();

        // TODO:

        throw new Error("Not implemented");
    }

    /**
     * Creates a GShell Artifact from a Mercury Artifact.
     */
    private Artifact createArtifact(final org.apache.maven.mercury.artifact.Artifact source) {
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

    /**
     * Creates a Mercury Artifact from a GShell Artifact.
     */
    private org.apache.maven.mercury.artifact.Artifact createArtifact(final Artifact source) {
        assert source != null;

        ArtifactBasicMetadata md = new ArtifactBasicMetadata();
        md.setGroupId(source.getGroup());
        md.setArtifactId(source.getName());
        md.setVersion(source.getVersion());
        md.setClassifier(source.getClassifier());
        md.setType(source.getType());

        org.apache.maven.mercury.artifact.Artifact artifact = new DefaultArtifact(md);

        artifact.setFile(source.getFile());

        return artifact;
    }
}