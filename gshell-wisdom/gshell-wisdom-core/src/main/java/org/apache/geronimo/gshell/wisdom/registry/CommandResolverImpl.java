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
import org.apache.geronimo.gshell.wisdom.command.AliasCommand;
import org.apache.geronimo.gshell.wisdom.command.GroupCommand;
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

    private FileObject aliasesDirectory;

    @PostConstruct
    public void init() throws Exception {
        assert fileSystemAccess != null;
        commandsDirectory = fileSystemAccess.resolveFile(null, "meta:/commands");
        aliasesDirectory = fileSystemAccess.resolveFile(null, "meta:/aliases");
    }

    public void setBeanContainer(final BeanContainer container) {
        assert container != null;

        this.container = container;
    }

    //
    // TODO: Consider adding an undefined command handler to allow for even more customization of
    //       execution when no defined command is found?  So one can say directly execute a
    //       *.gsh script, which under the covers will translate into 'source *.gsh' (or really
    //       should be 'shell *.gsh' once we have a sub-shell command.
    //
    
    public Command resolveCommand(final Variables vars, final String path) throws CommandException {
        assert vars != null;
        assert path != null;

        log.debug("Resolving command for path: {}", path);

        //
        // FIXME: For now just ask for the named stuff, eventually need a better path parser and lookup thingy
        //

        //
        // FIXME: Handle "/" to get to commandsDirectory.  Handle avoiding ../ leading to group dir set to meta:/ (should never go up past meta:/commands)
        //
                
        Command command = findAliasCommand(path);

        if (command == null) {
            try {
                FileObject dir = getGroupDirectory(vars);
                FileObject file = fileSystemAccess.resolveFile(dir, path);
                
                if (file.exists()) {
                    command = (Command) file.getContent().getAttribute("COMMAND");

                    // Dynamically create group commands
                    if (command == null && file.getType().hasChildren()) {
                        command = createGroupCommand(file);
                        file.getContent().setAttribute("COMMAND", command);
                    }
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

    private FileObject getGroupDirectory(final Variables vars) throws FileSystemException {
        assert vars != null;

        FileObject dir;

        Object tmp = vars.get("gshell.group");

        if (tmp == null) {
            assert commandsDirectory != null;
            dir = commandsDirectory;
        }
        else if (tmp instanceof String) {
            log.trace("Resolving group directory from string: {}", tmp);
            dir = fileSystemAccess.resolveFile(null, (String)tmp);
        }
        else if (tmp instanceof FileObject) {
            dir = (FileObject)tmp;
        }
        else {
            // Complain, then use the default so commands still work
            log.error("Invalid type for variable 'gshell.group'; expected String or FileObject; found: " + tmp.getClass());
            assert commandsDirectory != null;
            dir = commandsDirectory;
        }
        
        assert dir != null;
        return dir;
    }


    private Command findAliasCommand(final String path) throws CommandException {
        assert path != null;

        Command command = null;

        try {
            assert aliasesDirectory != null;
            FileObject file = fileSystemAccess.resolveFile(aliasesDirectory, path);
            if (file.exists()) {
                command = (Command)file.getContent().getAttribute("COMMAND");

                // Dynamically create alias commands
                if (command == null) {
                    command = createAliasCommand(file);
                    file.getContent().setAttribute("COMMAND", command);
                }
            }
        }
        catch (FileSystemException e) {
            throw new CommandException(e);
        }
        catch (NoSuchAliasException e) {
            // ignore
        }

        return command;
    }

    private Command createAliasCommand(final FileObject file) throws FileSystemException, NoSuchAliasException {
        assert file != null;

        String name = file.getName().getBaseName();
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
                Command command = (Command)file.getContent().getAttribute("COMMAND");

                // Dynamically create group commands
                if (command == null && file.getType().hasChildren()) {
                    command = createGroupCommand(file);
                }

                commands.add(command);
            }
        }
        catch (FileSystemException e) {
            throw new CommandException(e);
        }

        log.debug("Resolved {} commands: {}", commands.size(), commands);
        
        return commands;
    }

    private Command createGroupCommand(final FileObject file) throws FileSystemException {
        assert file != null;

        log.debug("Creating command for group: {}", file);

        GroupCommand command = new GroupCommand(file);

        //
        // FIXME: Have to inject the container because we are not wiring ^^^, and because its support muck needs some crap
        //        probably need to use a prototype here
        //

        assert container != null;
        command.setBeanContainer(container);

        return command;
    }
}
