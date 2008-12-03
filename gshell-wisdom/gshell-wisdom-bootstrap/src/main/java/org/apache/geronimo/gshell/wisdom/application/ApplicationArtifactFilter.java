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

import org.apache.geronimo.gshell.artifact.Artifact;
import org.apache.geronimo.gshell.artifact.ArtifactFilter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Artifact filter for applications.
 *
 * @version $Rev$ $Date$
 */
public class ApplicationArtifactFilter
    implements ArtifactFilter
{
    private static final String[] EXCLUDES = {
        "gshell-artifact",
        "gshell-ansi",
        "gshell-api",
        "gshell-application",
        "gshell-artifact",
        "gshell-chronos",
        "gshell-cli",
        "gshell-clp",
        "gshell-event",
        "gshell-i18n",
        "gshell-io",
        "gshell-spring",
        "gshell-terminal",
        "gshell-wisdom-bootstrap",
        "gshell-yarn",
        "jcl-over-slf4j",
        "jline",
        "log4j",
        "slf4j-api",
        "slf4j-log4j12",
        "spring-core",
        "spring-beans"
    };

    private final Set<String> excludes = new HashSet<String>();

    public ApplicationArtifactFilter() {
        excludes.addAll(Arrays.asList(EXCLUDES));
    }

    public boolean accept(final Artifact artifact) {
        assert artifact != null;

        return !excludes.contains(artifact.getName());
    }
}
