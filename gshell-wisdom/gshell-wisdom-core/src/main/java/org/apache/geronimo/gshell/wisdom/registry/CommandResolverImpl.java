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

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandException;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.commandline.CommandLineExecutor;
import org.apache.geronimo.gshell.registry.AliasRegistry;
import org.apache.geronimo.gshell.registry.CommandResolver;
import org.apache.geronimo.gshell.registry.NoSuchAliasException;
import org.apache.geronimo.gshell.registry.NoSuchCommandException;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.spring.BeanContainerAware;
import org.apache.geronimo.gshell.vfs.FileSystemAccess;
import org.apache.geronimo.gshell.vfs.FileObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@link CommandResolver} component.
 *
 * @version $Rev$ $Date$
 */
public class CommandResolverImpl
    implements CommandResolver, BeanContainerAware
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private AliasRegistry aliasRegistry;

    @Autowired
    private FileSystemAccess fileSystemAccess;

    @Autowired
    private CommandLineExecutor executor;

    private BeanContainer container;

    private FileObject commandsDirectory;

    @PostConstruct
    public void init() throws Exception {
        assert fileSystemAccess != null;
        commandsDirectory = fileSystemAccess.resolveFile(null, "meta:/commands");
    }

    public void setBeanContainer(final BeanContainer container) {
        assert container != null;

        this.container = container;
    }

    public Command resolveCommand(final Variables variables, final String path) throws CommandException {
        assert variables != null;
        assert path != null;

        log.debug("Resolving command for path: {}", path);

        //
        // FIXME: For now just ask for the named stuff, eventually need a better path parser and lookup thingy
        //

        Command command;

        assert aliasRegistry != null;
        if (aliasRegistry.containsAlias(path)) {
            command = createAliasCommand(path);
        }
        else {
            try {
                assert commandsDirectory != null;
                FileObject file = fileSystemAccess.resolveFile(commandsDirectory, path);
                if (file.exists()) {
                    command = (Command) file.getContent().getAttribute("COMMAND");
                }
                else {
                    throw new NoSuchCommandException(path);
                }

                FileObjects.close(file);
            }
            catch (FileSystemException e) {
                throw new CommandException(e);
            }
        }

        log.debug("Resolved command: {} -> {}", path, command);
        
        return command;
    }

    private Command createAliasCommand(final String name) throws NoSuchAliasException {
        assert name != null;

        log.debug("Creating command for alias: {}", name);

        assert aliasRegistry != null;
        String alias = aliasRegistry.getAlias(name);
        AliasCommand command = new AliasCommand(name, alias, executor);

        //
        // FIXME: Have to inject the container because we are not wiring ^^^, and because its support muck needs some crap
        //        probably need to use a prototype here
        //

        assert container != null;
        command.setBeanContainer(container);

        return command;
    }

    public Collection<Command> resolveCommands(final Variables variables, final String path) throws CommandException {
        assert variables != null;
        // for now path can be null

        log.debug("Resolving commands for path: {}", path);
        
        //
        // FIXME: For now ingore path, just return all commands under meta:/commands
        //
        
        List<Command> commands = new ArrayList<Command>();

        try {
            for (FileObject file : commandsDirectory.getChildren()) {
                // FIXME: For now ignore folders, not yet supported fully
                if (!file.getType().hasChildren()) {
                    Command command = (Command)file.getContent().getAttribute("COMMAND");
                    commands.add(command);
                }
            }
        }
        catch (FileSystemException e) {
            throw new CommandException(e);
        }

        log.debug("Resolved {} commands: {}", commands.size(), commands);
        
        return commands;
    }
}
