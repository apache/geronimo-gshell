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

package org.apache.geronimo.gshell.commands.vfs;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileUtil;

/**
 * Copy files.
 *
 * @version $Rev$ $Date$
 */
public class CopyCommand
    extends VFSCommandSupport
{
    public CopyCommand() {
        super("copy");
    }

    protected String getUsage() {
        return super.getUsage() + " <source> <target>";
    }

    protected boolean processCommandLine(final CommandLine line) throws CommandException {
        assert line != null;

        String[] args = line.getArgs();

        // Need exactly 2 args
        if (args.length != 2) {
            return true;
        }

        return false;
    }

    protected Object doExecute(Object[] args) throws Exception {
        assert args != null;

        FileSystemManager fsm = getFileSystemManager();
        FileObject source = fsm.resolveFile(String.valueOf(args[0]));
        FileObject target = fsm.resolveFile(String.valueOf(args[1]));

        log.info("Copying " + source + " -> " + target);

        FileUtil.copyContent(source, target);

        return Command.SUCCESS;
    }
}