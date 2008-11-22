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

package org.apache.geronimo.gshell.wisdom.registry;

import org.apache.geronimo.gshell.application.plugin.Plugin;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.registry.AliasRegistry;
import org.apache.geronimo.gshell.registry.CommandResolver;
import org.apache.geronimo.gshell.registry.NoSuchCommandException;
import org.apache.geronimo.gshell.spring.SpringTestSupport;
import org.apache.geronimo.gshell.wisdom.command.AliasCommand;

import java.util.Collection;

/**
 * Unit tests for the {@link CommandResolverImpl} class.
 *
 * @version $Rev$ $Date$
 */
public class CommandResolverImplTest
    extends SpringTestSupport
{
    private CommandResolverImpl resolver;

    private Variables vars;

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
            getDefaultComponentsConfigLocation(),
            getDefaultConfigLocation()
        };
    }

    protected void setUp() throws Exception {
        super.setUp();

        vars = new Variables();

        resolver = getBean(CommandResolverImpl.class);
        assertNotNull(resolver);

        Plugin plugin = getBean(Plugin.class);
        plugin.activate();
    }

    protected void tearDown() throws Exception {
        vars = null;
        resolver = null;
        
        super.tearDown();
    }

    /*
    public void testResolveRoot() throws Exception {
        Command command = resolver.resolveCommand("/", vars);
        assertNotNull(command);
        assertTrue(command instanceof GroupCommand);

        GroupCommand group = (GroupCommand)command;
        assertEquals("/", group.getPath());
    }
    */

    public void testResolveDefaultPath() throws Exception {
        Command command;

        command = resolver.resolveCommand("test1", vars);
        assertNotNull(command);

        command = resolver.resolveCommand("test2", vars);
        assertNotNull(command);

        command = resolver.resolveCommand("group1/child1", vars);
        assertNotNull(command);

        command = resolver.resolveCommand("group2/child2", vars);
        assertNotNull(command);

        try {
            resolver.resolveCommand("no-such-command", vars);
            fail();
        }
        catch (NoSuchCommandException ignore) {
            // expected
        }
    }

    public void testResolveCustomPath() throws Exception {
        vars.set(CommandResolver.PATH, "/:/group1");

        Command command;

        command = resolver.resolveCommand("test1", vars);
        assertNotNull(command);

        command = resolver.resolveCommand("test2", vars);
        assertNotNull(command);

        command = resolver.resolveCommand("child1", vars);
        assertNotNull(command);

        command = resolver.resolveCommand("child2", vars);
        assertNotNull(command);

        try {
            resolver.resolveCommand("no-such-command", vars);
            fail();
        }
        catch (NoSuchCommandException ignore) {
            // expected
        }
    }

    public void testResolveCustomPathInGroup() throws Exception {
        vars.set(CommandResolver.GROUP, "group1");
        vars.set(CommandResolver.PATH, "/:.");

        Command command;

        command = resolver.resolveCommand("test1", vars);
        assertNotNull(command);

        command = resolver.resolveCommand("test2", vars);
        assertNotNull(command);

        command = resolver.resolveCommand("child1", vars);
        assertNotNull(command);

        command = resolver.resolveCommand("child2", vars);
        assertNotNull(command);
    }

    /*
    public void testResolveInGroup() throws Exception {
        vars.set(CommandResolver.GROUP, "group1");
        vars.set(CommandResolver.PATH, "/:.");

        Command command;

        command = resolver.resolveCommand("child1", vars);
        assertNotNull(command);

        command = resolver.resolveCommand("child2", vars);
        assertNotNull(command);

        command = resolver.resolveCommand("../test1", vars);
        assertNotNull(command);
    }
    */
    
    public void testResolveRelativeChecks() throws Exception {
       try {
            resolver.resolveCommand("../foo", vars);
            fail();
        }
        catch (NoSuchCommandException ignore) {
            // expected
        }

        try {
            resolver.resolveCommand("../../bar", vars);
            fail();
        }
        catch (NoSuchCommandException ignore) {
            // expected
        }
        
        vars.set(CommandResolver.GROUP, "a/b/c/d");

        Command command;

        command = resolver.resolveCommand("../../../../test1", vars);
        assertNotNull(command);
    }

    public void testResolveCommands() throws Exception {
        Collection<Command> commands = resolver.resolveCommands(null, vars);
        assertNotNull(commands);
        assertEquals(6, commands.size());
    }

    public void testResolveAliases() throws Exception {
        AliasRegistry aliasRegistry = getBean(AliasRegistry.class);
        aliasRegistry.registerAlias("test", "test1");

        Command command;

        command = resolver.resolveCommand("test", vars);
        assertNotNull(command);
        assertTrue(command instanceof AliasCommand);
    }
}