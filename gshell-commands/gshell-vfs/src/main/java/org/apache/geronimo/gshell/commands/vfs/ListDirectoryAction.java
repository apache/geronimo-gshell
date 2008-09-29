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
import org.apache.commons.vfs.FileFilter;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileFilterSelector;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.io.PrintWriter;

import jline.ConsoleReader;

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

    //
    // TODO: Add -l support
    //

    @Option(name="-a")
    private boolean includeHidden = false;

    @Option(name="-r", aliases={"--recursive"})
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
            listChildren(io, file);
        }
        else {
            io.info("{}", file.getName());

            FileContent content = file.getContent();
            io.verbose("Size: {} bytes", content.getSize());

            DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
            String lastMod = dateFormat.format(new Date(content.getLastModifiedTime()));
            io.verbose("Last modified: {}", lastMod);
        }

        return Result.SUCCESS;
    }

    private void listChildren(final IO io, final FileObject dir) throws Exception {
        assert io != null;
        assert dir != null;

        FileObject[] files;

        if (includeHidden) {
            files = dir.getChildren();
        }
        else {
            FileFilter filter = new FileFilter() {
                public boolean accept(final FileSelectInfo selection) {
                    assert selection != null;

                    try {
                        return !selection.getFile().isHidden();
                    }
                    catch (FileSystemException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            files = dir.findFiles(new FileFilterSelector(filter));
        }

        //
        // FIXME: Need to have the framework provide a reader, which is initialized correctly... or make this accessible via IO?
        //

        ConsoleReader reader = new ConsoleReader(
                io.inputStream,
                new PrintWriter(io.outputStream, true),
                null, // bindings
                io.getTerminal());
        
        reader.setUsePagination(false);

        List<String> names = new ArrayList<String>(files.length);
        List<FileObject> dirs = new LinkedList<FileObject>();

        for (FileObject file : files) {
            names.add(file.getName().getBaseName());

            if (file.getType() == FileType.FOLDER) {
                names.add(file.getName().getBaseName() + "/");

                if (recursive) {
                    dirs.add(file);
                }
            }
        }

        reader.printColumns(names);
        
        if (!dirs.isEmpty()) {
            for (FileObject subdir : dirs) {
                io.out.println();
                io.out.print(subdir.getName().getBaseName());
                io.out.print(":");
                listChildren(io, subdir);
            }
        }
    }
}