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

package org.apache.geronimo.gshell.layout.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.geronimo.gshell.layout.LayoutManager;

/**
 * A group of nodes.
 *
 * @version $Rev$ $Date$
 */
@XStreamAlias("group")
public class GroupNode
    extends Node
{
    protected Set<Node> nodes = new HashSet<Node>();

    public GroupNode(final String name) {
        super(name);
    }
    
    public void add(final Node child) {
        assert child != null;

        child.setParent(this);

        nodes.add(child);
    }

    public Node find(final String name) {
        assert name != null;
        
        for (Node child : nodes) {
            if (name.equals(child.getName())) {
                return child;
            }
        }

        return null;
    }

    public Set<Node> nodes() {
        return Collections.unmodifiableSet(nodes);
    }

    public int size() {
        return nodes.size();
    }

    public boolean isEmpty() {
        return nodes == null || nodes.isEmpty();
    }
    
    /**
     * Link children to their parent when deserializing.
     */
    private Object readResolve() {
        if (!isEmpty()) {
            for (Node child : nodes) {
                child.setParent(this);
            }
        }

        return this;
    }
}