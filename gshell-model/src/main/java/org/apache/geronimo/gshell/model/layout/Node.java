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

package org.apache.geronimo.gshell.model.layout;

import org.apache.geronimo.gshell.model.Element;

/**
 * The rudimentary element of a layout.
 *
 * @version $Rev$ $Date$
 */
public abstract class Node
    extends Element
{
    public static final String SEPARATOR = "/";

    public static final String ROOT = "/";
    
    protected String name;

    protected transient Node parent;

    protected Node(final String name) {
        assert name != null;
        
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(final Node parent) {
        this.parent = parent;
    }

    public String getPath() {
        StringBuffer buff = new StringBuffer();
        Node node = this;

        while (node != null) {
            buff.insert(0, node.getName());

            node = node.getParent();

            if (node != null) {
                buff.insert(0, SEPARATOR);
            }

            if (node instanceof Layout) {
                break;
            }
        }

        return buff.toString();
    }
}
