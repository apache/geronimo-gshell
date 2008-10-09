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

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandException;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.registry.CommandResolver;
import org.apache.geronimo.gshell.registry.NoSuchCommandException;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.spring.BeanContainerAware;
import org.apache.geronimo.gshell.vfs.FileSystemAccess;
import org.apache.geronimo.gshell.vfs.provider.meta.MetaFileName;
import org.apache.geronimo.gshell.wisdom.command.GroupCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
    private FileSystemAccess fileSystemAccess;

    private FileObject commandsDirectory;

    private BeanContainer container;

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

    public Command resolveCommand(final String name, final Variables variables) throws CommandException {
        assert name != null;
        assert variables != null;

        log.debug("Resolving command name: {}", name);

        Command command = null;
        
        try {
            FileObject file = resolveCommandFile(name, variables);

            if (file != null) {
                command = createCommand(file);
            }
        }
        catch (FileSystemException e) {
            log.warn("Unable to resolve command for name: " + name, e);
        }

        if (command == null) {
            throw new NoSuchCommandException(name);
        }

        log.debug("Resolved command: {}", command);

        return command;
    }

    private FileObject resolveCommandFile(final String name, final Variables variables) throws FileSystemException {
        assert name != null;
        assert variables != null;

        log.debug("Resolving command file: {}", name);

        // Special handling for root
        if (name.equals("/")) {
            return getCommandsDirectory();
        }
        
        String[] searchPath = getSearchPath(variables);

        log.debug("Search path: {}", searchPath);

        FileObject groupDir = getGroupDirectory(variables);

        log.debug("Group dir: {}", groupDir);

        FileObject file = null;

        for (String pathElement : searchPath) {
            log.debug("Resolving file; name={}, pathElement={}", name, pathElement);

            FileObject dir = fileSystemAccess.resolveFile(null, groupDir.getName().getURI() + "/" + pathElement);

            log.debug("Dir: {}", dir);

            FileObject tmp = fileSystemAccess.resolveFile(dir, name);

            log.debug("File: {}", tmp);

            if (tmp.exists()) {
                file = tmp;
                break;
            }
        }

        if (file != null) {
            log.debug("Resolved file: {}", file);

            // Make sure whatever file we resolved is actually a meta file
            if (!isMetaFile(file)) {
                log.warn("Command name '{}' did not resolve to a meta-file; found: {}", name, file);
                return null;
            }

            // Make sure we found a file in the meta:/commands tree
            if (!file.getName().getPath().startsWith("/commands")) {
                log.warn("Command name '{}' did not resolve under " + COMMANDS_ROOT + "; found: {}", name, file);
                return null;
            }
        }

        return file;
    }

    private String[] getSearchPath(final Variables vars) {
        assert vars != null;

        Object tmp = vars.get(PATH);

        if (tmp instanceof String) {
            return ((String)tmp).split(PATH_SEPARATOR);
        }
        else if (tmp != null) {
            log.error("Invalid type for variable '" + PATH + "'; expected String; found: " + tmp.getClass());
        }

        // Return the default
        return new String[] { "/" };
    }

    public Collection<Command> resolveCommands(String name, Variables variables) throws CommandException {
        // name may be null
        assert variables != null;

        if (name == null) {
            name = "";
        }
        
        log.debug("Resolving commands for name: {}", name);

        List<Command> commands = new ArrayList<Command>();

        try {
            FileObject file = resolveCommandFile(name, variables);

            log.debug("Resolved (for commands): {}", file);

            if (file != null && file.exists()) {
                if (file.getType().hasChildren()) {
                    for (FileObject child : file.getChildren()) {
                        Command command = createCommand(child);
                        commands.add(command);
                    }
                }
                else {
                    Command command = createCommand(file);
                    commands.add(command);
                }
            }
        }
        catch (FileSystemException e) {
            log.warn("Failed to resolve commands for name: " + name, e);
        }

        log.debug("Resolved {} commands", commands.size());
        if (log.isTraceEnabled()) {
            for (Command command : commands) {
                log.trace("    {}", command);
            }
        }

        return commands;
    }

    private boolean isMetaFile(final FileObject file) {
        assert file != null;

        return MetaFileName.SCHEME.equals(file.getName().getScheme());
    }

    private FileObject getCommandsDirectory() throws FileSystemException {
        if (commandsDirectory == null) {
            commandsDirectory = fileSystemAccess.resolveFile(null, COMMANDS_ROOT);
        }

        return commandsDirectory;
    }

    private FileObject getGroupDirectory(final Variables vars) throws FileSystemException {
        assert vars != null;

        FileObject dir;

        Object tmp = vars.get(GROUP);

        if (tmp == null) {
            dir = getCommandsDirectory();
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
            log.error("Invalid type for variable '" + GROUP + "'; expected String or FileObject; found: " + tmp.getClass());
            dir = getCommandsDirectory();
        }

        if (!isMetaFile(dir)) {
            log.error("Command group did not resolve to a meta-file: {}", dir);
            dir = getCommandsDirectory();
        }

        return dir;
    }

    private Command createCommand(final FileObject file) throws FileSystemException, CommandException {
        assert file != null;

        log.debug("Creating command for file: {}", file);

        Command command = null;

        if (file.exists()) {
            FileContent content = file.getContent();
            command = (Command)content.getAttribute("COMMAND");

            if (command == null) {
                if (file.getType().hasChildren()) {
                    command = createGroupCommand(file);
                    content.setAttribute("COMMAND", command);
                }

                // TODO: Try to construct AliasCommand?
            }
        }

        if (command == null) {
            throw new CommandException("Unable to create command for file: " + file.getName());
        }

        return command;
    }

    /*
    private Command createAliasCommand(final FileObject file) throws FileSystemException {
        assert file != null;

        String name = file.getName().getBaseName();

        log.debug("Creating command for alias: {}", name);

        AliasCommand command = container.getBean(AliasCommand.class);

        String alias = (String) file.getContent().getAttribute("ALIAS");
        if (alias == null) {
            throw new IllegalStateException("Alias meta-file does not contain 'ALIAS' attribute: " + file);
        }

        command.setName(name);
        command.setAlias(alias);

        return command;
    }
    */

    private Command createGroupCommand(final FileObject file) throws FileSystemException {
        assert file != null;

        log.debug("Creating command for group: {}", file);

        GroupCommand command = container.getBean(GroupCommand.class);
        command.setFile(file);
        
        return command;
    }
}
