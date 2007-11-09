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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.Annotations;
import junit.framework.TestCase;

/**
 * Test for the {@link GroupNode} class.
 *
 * @version $Rev$ $Date$
 */
public class GroupNodeTest
    extends TestCase
{
    public void testAddLinks() throws Exception {
        GroupNode group = new GroupNode("test");

        Node n1 = new CommandNode("a", "b");

        group.add(n1);

        assertEquals(group, n1.getParent());
    }

    public void testDeserializeLinks() throws Exception {
        GroupNode g1 = new GroupNode("test");

        Node n1 = new CommandNode("a", "b");

        g1.add(n1);

        XStream xs = new XStream();

        Annotations.configureAliases(xs, GroupNode.class);
        
        String xml = xs.toXML(g1);

        System.err.println("XML: " + xml);

        GroupNode g2 = (GroupNode) xs.fromXML(xml);

        Node n2 = g2.nodes().iterator().next();
        
        assertEquals(g2, n2.getParent());
    }
}