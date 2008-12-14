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

import org.apache.maven.mercury.metadata.MetadataTreeArtifactFilter;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Filters artifacts required for using Maven Murcury for resolution of artifacts.
 *
 * @version $Rev$ $Date$
 */
public class MercuryDependenciesFilter
    implements MetadataTreeArtifactFilter
{
    /*
    [INFO] org.apache.geronimo.gshell.support:gshell-artifact-mercury:jar:1.0-alpha-2-SNAPSHOT
    [INFO] +- org.slf4j:slf4j-api:jar:1.5.6:compile
    [INFO] +- org.apache.geronimo.gshell.support:gshell-artifact:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] |  \- org.apache.geronimo.gshell.support:gshell-io:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] |     +- org.apache.geronimo.gshell.support:gshell-yarn:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] |     \- org.apache.geronimo.gshell.support:gshell-ansi:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] |        +- org.apache.geronimo.gshell.support:gshell-i18n:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] |        \- org.apache.geronimo.gshell.support:gshell-terminal:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] |           \- jline:jline:jar:0.9.94:compile
    [INFO] +- org.apache.maven.mercury:mercury-md-sat:jar:1.0.0-alpha-2:compile
    [INFO] |  +- org.sat4j:org.sat4j.core:jar:2.0.4:compile
    [INFO] |  +- org.sat4j:org.sat4j.pb:jar:2.0.4:compile
    [INFO] |  +- org.apache.maven.mercury:mercury-md-shared:jar:1.0.0-alpha-2:compile
    [INFO] |  |  +- org.apache.maven.mercury:mercury-artifact:jar:1.0.0-alpha-2:compile
    [INFO] |  |  +- org.apache.maven.mercury:mercury-util:jar:1.0.0-alpha-2:compile
    [INFO] |  |  \- org.apache.maven.mercury:mercury-crypto-basic:jar:1.0.0-alpha-2:compile
    [INFO] |  |     \- org.apache.maven.mercury:mercury-crypto-api:jar:1.0.0-alpha-2:compile
    [INFO] |  +- org.apache.maven.mercury:mercury-repo-virtual:jar:1.0.0-alpha-2:compile
    [INFO] |  |  \- org.apache.maven.mercury:mercury-repo-cache-fs:jar:1.0.0-alpha-2:compile
    [INFO] |  |     \- org.apache.maven.mercury:mercury-repo-api:jar:1.0.0-alpha-2:compile
    [INFO] |  |        +- org.apache.maven.mercury:mercury-transport-api:jar:1.0.0-alpha-2:compile
    [INFO] |  |        \- org.apache.maven.mercury:mercury-external:jar:1.0.0-alpha-2:compile
    [INFO] |  +- org.apache.maven.mercury:mercury-logging:jar:1.0.0-alpha-2:compile
    [INFO] |  +- org.apache.maven.mercury:mercury-event:jar:1.0.0-alpha-2:compile
    [INFO] |  \- org.codehaus.plexus:plexus-lang:jar:1.1:compile
    [INFO] |     +- org.codehaus.plexus:plexus-utils:jar:1.5.5:compile
    [INFO] |     \- org.codehaus.plexus:plexus-container-default:jar:1.0-beta-3.0.1:compile
    [INFO] \- junit:junit:jar:3.8.2:test (scope not updated to compile)
    */

    private static final String[] EXCLUDES = {
        "gshell-artifact-mercury",
        "mercury-md-sat",
        "org.sat4j.core",
        "org.sat4j.pb",
        "mercury-md-shared",
        "mercury-artifact",
        "mercury-util",
        "mercury-crypto-basic",
        "mercury-crypto-api",
        "mercury-repo-virtual",
        "mercury-repo-cache-fs",
        "mercury-repo-api",
        "mercury-transport-api",
        "mercury-external",
        "mercury-logging",
        "mercury-event",
        "plexus-lang",
        "plexus-utils",
        "plexus-container-default",
    };

    private final Set<String> excludes = new HashSet<String>();

    public MercuryDependenciesFilter() {
        excludes.addAll(Arrays.asList(EXCLUDES));
    }

    public boolean veto(final ArtifactBasicMetadata md) {
        assert md != null;

        return !excludes.contains(md.getArtifactId());
    }
}