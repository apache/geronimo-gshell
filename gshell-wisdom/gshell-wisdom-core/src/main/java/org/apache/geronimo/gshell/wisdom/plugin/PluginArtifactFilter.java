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

package org.apache.geronimo.gshell.wisdom.plugin;

import org.apache.geronimo.gshell.application.Application;
import org.apache.geronimo.gshell.wisdom.application.ApplicationArtifactFilter;
import org.apache.geronimo.gshell.artifact.Artifact;

import java.util.HashSet;
import java.util.Set;

/**
 * Artifact filter for plugins.
 *
 * @version $Rev$ $Date$
 */
public class PluginArtifactFilter
    extends ApplicationArtifactFilter
{
    private final Set<String> excludes = new HashSet<String>();

    public PluginArtifactFilter(final Application application) {
        assert application != null;

        // Filter out application artifacts, need to use gid:aid to make sure we don't clober anything which has the same artifactId, but different groupId
        for (Artifact artifact : application.getClassPath().getArtifacts()) {
            String id = artifact.getGroup() + ":" + artifact.getName();
            excludes.add(id);
        }
    }

    @Override
    public boolean accept(final Artifact artifact) {
        if (super.accept(artifact)) {
            return !excludes.contains(artifact.getId());
        }

        return false;
    }
}