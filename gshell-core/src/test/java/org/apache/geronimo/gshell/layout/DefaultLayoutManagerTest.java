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
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.IO;
import org.apache.geronimo.gshell.layout.loader.XMLLayoutLoader;
import org.apache.geronimo.gshell.layout.model.Layout;
import org.apache.geronimo.gshell.layout.model.CommandNode;
import org.apache.geronimo.gshell.layout.model.AliasNode;
import org.apache.geronimo.gshell.layout.model.GroupNode;
import org.apache.geronimo.gshell.registry.CommandRegistry;
import org.apache.geronimo.gshell.registry.DefaultCommandRegistry;
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

    private CommandRegistry registry;

    private Layout layout;

    private Environment env;

    protected void setUp() throws Exception {
        registry = new DefaultCommandRegistry();
        env = new DefaultEnvironment(new IO());

        layout = new Layout();
        layout.add(new CommandNode("help", "help"));
        GroupNode g = new GroupNode("test");
        g.add(new CommandNode("foo", "foo"));
        layout.add(g);

        layoutManager = new DefaultLayoutManager(registry, layout, env);
        // layoutManager.initialize();
    }

    public void testFind() throws Exception {
        registry.register(new Command() {
            public String getId() {
                return "help";
            }

            public String getDescription() {
                return null;
            }

            public Object execute(CommandContext context, Object... args) throws Exception {
                return null;
            }
        });

        layoutManager.find("help");
    }

    public void testFindInGroup() throws Exception {
        registry.register(new Command() {
            public String getId() {
                return "foo";
            }

            public String getDescription() {
                return null;
            }

            public Object execute(CommandContext context, Object... args) throws Exception {
                return null;
            }
        });

        layoutManager.find("test/foo");
    }

    public void testFindNotFound() throws Exception {
        try {
            layoutManager.find("no-such-command");
            fail();
        }
        catch(NotFoundException expected) {}
    }
}