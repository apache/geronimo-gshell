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
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.registry.CommandResolver;
import org.apache.geronimo.gshell.shell.ShellContextHolder;
import org.apache.geronimo.gshell.vfs.FileSystemAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves to location of <tt>gshell.group</tt>.
 *
 * @version $Rev$ $Date$
 */
public class GroupDirectoryResolver
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final FileSystemAccess fileSystemAccess;

    private FileObject commandsRoot;

    public GroupDirectoryResolver(final FileSystemAccess fileSystemAccess) {
        assert fileSystemAccess != null;
        this.fileSystemAccess = fileSystemAccess;
    }

    private FileObject getCommandsRoot() throws FileSystemException {
        if (commandsRoot == null) {
            commandsRoot = fileSystemAccess.createVirtualFileSystem(CommandResolver.COMMANDS_ROOT);
        }

        return commandsRoot;
    }

    public FileObject getGroupDirectory(final Variables vars) throws FileSystemException {
        assert vars != null;

        FileObject root = getCommandsRoot();
        FileObject dir = null;

        Object tmp = vars.get(CommandResolver.GROUP);

        if (tmp instanceof String) {
            log.trace("Resolving group directory from string: {}", tmp);

            dir = root.resolveFile((String)tmp);
        }
        else if (tmp != null) {
            // Complain, then use the default so commands still work
            log.error("Invalid type for variable '" + CommandResolver.GROUP + "'; expected String; found: " + tmp.getClass());
        }

        if (dir == null) {
            dir = root;
        }

        return dir;
    }

    public FileObject getGroupDirectory() throws FileSystemException {
        return getGroupDirectory(ShellContextHolder.get().getVariables());
    }
}