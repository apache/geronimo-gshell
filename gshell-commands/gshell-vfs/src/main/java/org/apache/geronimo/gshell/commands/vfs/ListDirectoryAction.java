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

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;

import java.text.DateFormat;
import java.util.Date;

/**
 * List the contents of a file or directory.
 *
 * @version $Rev$ $Date$
 */
public class ListDirectoryAction
    extends VfsActionSupport
{
    @Argument
    private String path;

    @Option(name="-r", aliases={ "--recursive" })
    private boolean recursive = false;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        FileObject file;
        if (path != null) {
            file = resolveFile(context, path);
        }
        else {
            file = getCurrentDirectory(context);
        }

        if (file.getType() == FileType.FOLDER) {
            listChildren(io, file, recursive, "");
        }
        else {
            io.info("{}", file.getName());

            FileContent content = file.getContent();
            io.info("Size: {} bytes", content.getSize());

            DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
            String lastMod = dateFormat.format(new Date(content.getLastModifiedTime()));
            io.info("Last modified: {}", lastMod);
        }

        return Result.SUCCESS;
    }

    private void listChildren(final IO io, final FileObject dir, final boolean recursive, final String prefix) throws FileSystemException {
        assert io != null;
        assert dir != null;
        assert prefix != null;

        for (FileObject child : dir.getChildren()) {
            io.out.print(prefix);
            io.out.print(child.getName().getBaseName());

            if (child.getType() == FileType.FOLDER) {
                io.out.println("/");

                if (recursive) {
                    listChildren(io, child, recursive, prefix + "    ");
                }
            }
            else {
                io.out.println();
            }
        }
    }
}