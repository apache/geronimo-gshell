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

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Dependency artifact configuration.
 *
 * @version $Rev$ $Date$
 */
@XStreamAlias("dependency")
public class Dependency
    extends DependencySupport
{
    private transient DependencyGroup dependencyGroup;

    public DependencyGroup getDependencyGroup() {
        return dependencyGroup;
    }

    public void setDependencyGroup(final DependencyGroup group) {
        this.dependencyGroup = group;
    }

    // Return configuration detals from the group if not directly configured
    
    public String getGroupId() {
        String tmp = super.getGroupId();

        if (tmp == null && dependencyGroup != null) {
            tmp = dependencyGroup.getGroupId();
        }

        return tmp;
    }

    public String getArtifactId() {
        String tmp = super.getArtifactId();

        if (tmp == null && dependencyGroup != null) {
            tmp = dependencyGroup.getArtifactId();
        }

        return tmp;
    }

    public String getClassifier() {
        String tmp = super.getClassifier();

        if (tmp == null && dependencyGroup != null) {
            tmp = dependencyGroup.getClassifier();
        }

        return tmp;
    }

    public String getType() {
        String tmp = super.getType();

        if (tmp == null && dependencyGroup != null) {
            tmp = dependencyGroup.getType();
        }

        return tmp;
    }

    public String getVersion() {
        String tmp = super.getVersion();

        if (tmp == null && dependencyGroup != null) {
            tmp = dependencyGroup.getVersion();
        }

        return tmp;
    }
}