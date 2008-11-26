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

import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.Artifact;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Filters artifacts required for using Maven Artifact for resolution of artifacts.
 *
 * @version $Rev$ $Date$
 */
public class MavenArtifactDependenciesFilter
    implements ArtifactFilter
{
    private static final String[] EXCLUDES = {
        "gshell-artifact-maven",
        "gshell-plexus",
        "aspectjrt",
        "maven-artivact",
        "maven-model",
        "maven-plugin-registry",
        "maven-profile",
        "maven-project",
        "maven-settings",
        "plexus-classworlds",
        "plexus-component-annotations",
        "plexus-container-default",
        "plexus-interpolation",
        "plexus-utils",
        "wagon-file",
        "wagon-http-lightweight",
        "wagon-http-shared",
        "wagon-provider-api",
        "xbean-reflect",
        "xercesMinimal",
        "nekohtml",

    };

    private final Set<String> excludes = new HashSet<String>();

    public MavenArtifactDependenciesFilter() {
        excludes.addAll(Arrays.asList(EXCLUDES));
    }

    public boolean include(final Artifact artifact) {
        assert artifact != null;

        String name = artifact.getArtifactId();

        return !excludes.contains(name);
    }
}