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

import junit.framework.TestCase;

/**
 * Test for the {@link org.apache.geronimo.gshell.model.layout.Node} class.
 *
 * @version $Rev$ $Date$
 */
public class NodeTest
    extends TestCase
{
    public void testGetPath1() throws Exception {
        Layout layout = new Layout();

        CommandNode c = new CommandNode("foo", "bar");
        layout.add(c);

        assertEquals("/foo", c.getPath());
    }

    public void testGetPath2() throws Exception {
        Layout layout = new Layout();

        GroupNode g = new GroupNode("test");
        layout.add(g);

        CommandNode c = new CommandNode("foo", "bar");
        g.add(c);

        assertEquals("/test/foo", c.getPath());
    }

    public void testGetPath3() throws Exception {
        Layout layout = new Layout();

        GroupNode g1 = new GroupNode("a");
        layout.add(g1);

        GroupNode g2 = new GroupNode("b");
        g1.add(g2);

        CommandNode c = new CommandNode("foo", "bar");
        g2.add(c);

        assertEquals("/a/b/foo", c.getPath());
    }
}