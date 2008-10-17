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

package org.apache.geronimo.gshell.wisdom.application;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExclusionSetFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;

/**
 * Artifact filter for applications.
 *
 * @version $Rev$ $Date$
 */
public class ApplicationArtifactFilter
    implements ArtifactFilter
{
    private ArtifactFilter delegate;

    protected AndArtifactFilter createFilter() {
        AndArtifactFilter filter = new AndArtifactFilter();

        filter.add(new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME));

        // Exclude bootstrap classes (stuff which lives in ${gshell.home}/lib
        filter.add(new ExclusionSetFilter(new String[] {
            //
            // FIXME: Load this list from build-generated properties or something like that
            //

            "aopalliance",
            "aspectjrt",
            "geronimo-annotation_1.0_spec",
            "gshell-ansi",
            "gshell-api",
            "gshell-artifact",
            "gshell-application",
            "gshell-cli",
            "gshell-clp",
            "gshell-chronos",
            "gshell-i18n",
            "gshell-io",
            "gshell-model",
            "gshell-spring",
            "gshell-wisdom-bootstrap",
            "gshell-yarn",
            "jcl104-over-slf4j",
            "gshell-terminal",
            "jline",
            "log4j",
            "maven-artifact",
            "maven-model",
            "maven-profile",
            "maven-project",
            "maven-workspace",
            "maven-settings",
            "maven-plugin-registry",
            "plexus-component-annotations",
            "plexus-container-default",
            "plexus-interpolation",
            "plexus-utils",
            "plexus-classworlds",
            "slf4j-api",
            "slf4j-log4j12",
            "spring-core",
            "spring-context",
            "spring-beans",
            "wagon-file",
            "wagon-http-lightweight",
            "wagon-http-shared",
            "wagon-provider-api",
            "xbean-reflect"
        }));

        return filter;
    }

    public final boolean include(final Artifact artifact) {
        if (delegate == null) {
            delegate = createFilter();
        }
        
        return delegate.include(artifact);
    }
}
