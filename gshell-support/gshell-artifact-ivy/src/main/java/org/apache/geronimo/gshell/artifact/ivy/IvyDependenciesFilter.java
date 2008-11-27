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

package org.apache.geronimo.gshell.artifact.ivy;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.util.filter.Filter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Filters artifacts required for using Apache Ivy for resolution of artifacts.
 *
 * @version $Rev$ $Date$
 */
public class IvyDependenciesFilter
    implements Filter
{
    /*
    [INFO] org.apache.geronimo.gshell.support:gshell-artifact-ivy:jar:1.0-alpha-2-SNAPSHOT
    [INFO] +- org.apache.geronimo.gshell.support:gshell-io:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] |  +- org.slf4j:slf4j-api:jar:1.5.6:compile
    [INFO] |  +- org.apache.geronimo.gshell.support:gshell-yarn:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] |  \- org.apache.geronimo.gshell.support:gshell-ansi:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] |     +- org.apache.geronimo.gshell.support:gshell-i18n:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] |     \- org.apache.geronimo.gshell.support:gshell-terminal:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] |        \- jline:jline:jar:0.9.94:compile
    [INFO] +- org.apache.geronimo.gshell.support:gshell-spring:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] |  +- org.slf4j:jcl-over-slf4j:jar:1.5.6:compile
    [INFO] |  +- org.apache.geronimo.gshell.support:gshell-chronos:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] |  +- org.springframework:spring-core:jar:2.5.5:compile
    [INFO] |  \- org.springframework:spring-beans:jar:2.5.5:compile
    [INFO] +- org.apache.geronimo.gshell.support:gshell-artifact:jar:1.0-alpha-2-SNAPSHOT:compile
    [INFO] +- org.apache.ivy:ivy:jar:2.0.0-rc2:compile
    [INFO] +- org.slf4j:slf4j-log4j12:jar:1.5.6:test
    [INFO] |  \- log4j:log4j:jar:1.2.15:test (version managed from 1.2.14)
    [INFO] +- org.apache.geronimo.gshell.support:gshell-spring:jar:tests:1.0-alpha-2-SNAPSHOT:test
    [INFO] \- junit:junit:jar:3.8.2:test
    */

    private static final String[] EXCLUDES = {
        "gshell-artifact-ivy",
        "ivy",
    };

    private final Set<String> excludes = new HashSet<String>();

    public IvyDependenciesFilter() {
        excludes.addAll(Arrays.asList(EXCLUDES));
    }

    public boolean accept(final Object obj) {
        if (!(obj instanceof Artifact)) {
            return false;
        }

        Artifact artifact = (Artifact)obj;
        String name = artifact.getName();

        return !excludes.contains(name);
    }
}