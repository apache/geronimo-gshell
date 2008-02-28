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

import junit.framework.TestCase;

/**
 * Tests for the {@link Layout} class.
 *
 * @version $Rev$ $Date$
 */
public class LayoutTest
    extends TestCase
{
    public void testReadLayout1() throws Exception {
        Layout layout = LayoutMarshaller.unmarshal(getClass().getResourceAsStream("layout1.xml"));
        assertNotNull(layout);
    }

    public void testReadLayout2() throws Exception {
        Layout layout = LayoutMarshaller.unmarshal(getClass().getResourceAsStream("layout2.xml"));
        assertNotNull(layout);
    }
    
    public void testDumpLayout1() throws Exception {
        Layout layout = new Layout();
        
        layout.add(new CommandNode("foo", "bar"));
        layout.add(new AliasNode("f", "foo"));

        GroupNode g = new GroupNode("test");
        g.add(new CommandNode("a", "b"));
        g.add(new CommandNode("c", "d"));

        layout.add(g);
        
        String xml = LayoutMarshaller.marshal(layout);

        System.err.println("XML: " + xml);
    }
}
