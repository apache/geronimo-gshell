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

package org.apache.geronimo.gshell.model.common;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Defines an artifact (groupId, artifactId, version, etc).
 *
 * @version $Rev$ $Date$
 */
public class Artifact
    extends ArtifactSupport
{
    @XStreamOmitField
    private ArtifactGroup artifactGroup;

    public ArtifactGroup getArtifactGroup() {
        return artifactGroup;
    }

    public void setArtifactGroup(final ArtifactGroup artifactGroup) {
        this.artifactGroup = artifactGroup;
    }

    @Override
    public String getGroupId() {
        String tmp = super.getGroupId();

        if (tmp == null && artifactGroup != null) {
            tmp = artifactGroup.getGroupId();
        }

        return tmp;
    }

    @Override
    public String getArtifactId() {
        String tmp = super.getArtifactId();

        if (tmp == null && artifactGroup != null) {
            tmp = artifactGroup.getArtifactId();
        }

        return tmp;
    }

    @Override
    public String getClassifier() {
        String tmp = super.getClassifier();

        if (tmp == null && artifactGroup != null) {
            tmp = artifactGroup.getClassifier();
        }

        return tmp;
    }

    @Override
    public String getType() {
        String tmp = super.getType();

        if (tmp == null && artifactGroup != null) {
            tmp = artifactGroup.getType();
        }

        return tmp;
    }

    @Override
    public String getVersion() {
        String tmp = super.getVersion();

        if (tmp == null && artifactGroup != null) {
            tmp = artifactGroup.getVersion();
        }

        return tmp;
    }
}