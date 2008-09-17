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

import java.util.List;

/**
 * Support for {@link Artifact} groups.
 *
 * @version $Rev$ $Date$
 */
public abstract class ArtifactGroup<T extends Artifact>
    extends ArtifactSupport
{
    public abstract List<T> getArtifacts();

    public void add(final T artifact) {
        assert artifact != null;

        getArtifacts().add(artifact);
    }

    public int size() {
        return getArtifacts().size();
    }

    public boolean isEmpty() {
        return getArtifacts().isEmpty();
    }

    /**
     * Link children to their parent group when deserializing.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    private Object readResolve() {
        if (!isEmpty()) {
            for (Artifact child : getArtifacts()) {
                child.setArtifactGroup(this);
            }
        }

        return this;
    }
}