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
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;
import java.util.ArrayList;

/**
 * Groups dependency elements to allow artifact configuration to be shared.
 *
 * @version $Rev$ $Date$
 */
@XStreamAlias("dependencyGroup")
public class DependencyGroup
    extends DependencySupport
{
    @XStreamImplicit
    private List<Dependency> dependencies;

    public List<Dependency> dependencies() {
        if (dependencies == null) {
            dependencies = new ArrayList<Dependency>();
        }

        return dependencies;
    }
    
    public void add(final Dependency dependency) {
        assert dependency != null;

        dependencies().add(dependency);
    }

    public int size() {
        return dependencies().size();
    }
    
    public boolean isEmpty() {
        return dependencies().isEmpty();
    }

    /**
     * Link children to their parent group when deserializing.
     */
    private Object readResolve() {
        if (!isEmpty()) {
            for (Dependency child : dependencies()) {
                child.setDependencyGroup(this);
            }
        }

        return this;
    }
}