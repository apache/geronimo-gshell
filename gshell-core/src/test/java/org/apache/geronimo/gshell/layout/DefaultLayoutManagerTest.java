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

package org.apache.geronimo.gshell.layout;

import junit.framework.TestCase;
import org.apache.geronimo.gshell.DefaultEnvironment;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.layout.loader.DummyLayoutLoader;
import org.apache.geronimo.gshell.layout.model.CommandNode;
import org.apache.geronimo.gshell.layout.model.GroupNode;
import org.apache.geronimo.gshell.layout.model.Layout;
import org.apache.geronimo.gshell.layout.model.Node;
import org.apache.geronimo.gshell.shell.Environment;

/**
 * Tests for the {@link DefaultLayoutManager} class.
 *
 * @version $Rev$ $Date$
 */
public class DefaultLayoutManagerTest
    extends TestCase
{
    private DefaultLayoutManager layoutManager;

    protected void setUp() throws Exception {
        Environment env = new DefaultEnvironment(new IO());

        Layout layout = new Layout();
        layout.add(new CommandNode("help", "help"));
        GroupNode g = new GroupNode("test");
        g.add(new CommandNode("foo", "foo"));
        layout.add(g);

        layoutManager = new DefaultLayoutManager(new DummyLayoutLoader(layout), env);
        layoutManager.initialize();
    }

    public void testFind() throws Exception {
        Node node = layoutManager.findNode("help");
        assertNotNull(node);
    }

    public void testFindInGroup() throws Exception {
        Node node = layoutManager.findNode("test/foo");
        assertNotNull(node);
    }
    
    public void testFindWithCommandPath() throws Exception {
        Node node = layoutManager.findNode("foo", "/:test");
        assertNotNull(node);
    }

    public void testFindNotFound() throws Exception {
        try {
            layoutManager.findNode("no-such-command");
            fail();
        }
        catch(NotFoundException expected) {}
    }
}