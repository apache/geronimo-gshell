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

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;

/**
 * Changes the current directory.
 *
 * @version $Rev$ $Date$
 */
public class ChangeDirectoryAction
    extends VfsActionSupport
{
    @Argument
    private String path;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        if (path == null) {
            // TODO: May need to ask the Application for this, as it might be different depending on the context (ie. remote user, etc)
            path = System.getProperty("user.home");
        }

        FileObject file = resolveFile(context, path);

        // Complain if the file is missing or is not a directory
        if (!file.exists()) {
            io.error("Directory not found: {}", file.getName());
            return Result.FAILURE;
        }
        else if (file.getType() != FileType.FOLDER) {
            io.error("File is not a directory: {}", file.getName());
            return Result.FAILURE;
        }

        setCurrentDirectory(context, file);
        
        return Result.SUCCESS;
    }
}