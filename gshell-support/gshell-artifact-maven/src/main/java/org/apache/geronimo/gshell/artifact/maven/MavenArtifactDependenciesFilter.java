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
    /*
    [INFO] org.apache.geronimo.gshell.support:gshell-artifact-maven:jar:1.0-alpha-2-SNAPSHOT
    [INFO] +- org.slf4j:slf4j-api:jar:1.5.6:compile
    [INFO] +- org.slf4j:slf4j-log4j12:jar:1.5.6:test
    [INFO] |  \- log4j:log4j:jar:1.2.15:test (version managed from 1.2.14)
    [INFO] +- org.apache.geronimo.gshell.support:gshell-chronos:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] |  \- org.apache.geronimo.gshell.support:gshell-yarn:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] +- org.apache.geronimo.gshell.support:gshell-io:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] |  \- org.apache.geronimo.gshell.support:gshell-ansi:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] |     +- org.apache.geronimo.gshell.support:gshell-i18n:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] |     \- org.apache.geronimo.gshell.support:gshell-terminal:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] |        \- jline:jline:jar:0.9.94:compile
    [INFO] +- org.apache.geronimo.gshell.support:gshell-spring:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] |  +- org.slf4j:jcl-over-slf4j:jar:1.5.6:compile
    [INFO] |  +- org.springframework:spring-core:jar:2.5.5:compile
    [INFO] |  \- org.springframework:spring-beans:jar:2.5.5:compile
    [INFO] +- org.apache.geronimo.gshell.support:gshell-artifact:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] +- org.apache.geronimo.gshell.support:gshell-plexus:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] |  +- org.codehaus.plexus:plexus-container-default:jar:1.0-beta-2:compile
    [INFO] |  |  +- org.codehaus.plexus:plexus-classworlds:jar:1.3:compile
    [INFO] |  |  +- org.apache.xbean:xbean-reflect:jar:3.4:compile
    [INFO] |  |  \- com.google.code.google-collections:google-collect:jar:snapshot-20080530:compile
    [INFO] |  +- org.codehaus.plexus:plexus-component-annotations:jar:1.0-beta-2:compile
    [INFO] |  \- org.codehaus.plexus:plexus-utils:jar:1.5.6:compile
    [INFO] +- org.apache.maven.artifact:maven-artifact:jar:3.0-alpha-1:compile
    [INFO] |  \- aspectj:aspectjrt:jar:1.5.3:compile
    [INFO] +- org.apache.maven:maven-project:jar:2.1.0-M2-SNAPSHOT:compile
    [INFO] |  +- org.apache.maven:maven-settings:jar:2.1.0-M2-SNAPSHOT:compile
    [INFO] |  +- org.apache.maven:maven-profile:jar:2.1.0-M2-SNAPSHOT:compile
    [INFO] |  +- org.apache.maven:maven-model:jar:2.1.0-M2-SNAPSHOT:compile
    [INFO] |  +- org.apache.maven:maven-plugin-registry:jar:2.1.0-M2-SNAPSHOT:compile
    [INFO] |  \- org.codehaus.plexus:plexus-interpolation:jar:1.5:compile
    [INFO] +- org.apache.maven.wagon:wagon-provider-api:jar:1.0-beta-4:compile
    [INFO] +- org.apache.maven.wagon:wagon-file:jar:1.0-beta-4:runtime
    [INFO] +- org.apache.maven.wagon:wagon-http-lightweight:jar:1.0-beta-4:runtime
    [INFO] |  \- org.apache.maven.wagon:wagon-http-shared:jar:1.0-beta-4:runtime
    [INFO] |     +- nekohtml:xercesMinimal:jar:1.9.6.2:runtime
    [INFO] |     \- nekohtml:nekohtml:jar:1.9.6.2:runtime
    [INFO] +- org.apache.geronimo.gshell.support:gshell-spring:jar:tests:1.0-alpha-2-SNAPSHOT:test
    [INFO] \- junit:junit:jar:3.8.2:test
    */
    
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
        "google-collect",
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