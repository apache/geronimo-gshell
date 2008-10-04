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
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.util.Os;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.vfs.provider.local.LocalFile;
import org.apache.geronimo.gshell.vfs.provider.local.LocalFileSystem;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Edit a file with an external editor.
 *
 * @version $Rev$ $Date$
 */
public class EditAction
    extends VfsActionSupport
{
    @Option(name="-e", aliases={"--editor"})
    private String editor;
    
    @Argument(required=true)
    private String path;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        FileObject file = resolveFile(context, path);

        if (!file.exists()) {
            // TODO: Allow creation of files which don't exist
            io.error("File not found: {}", file.getName());
            return Result.FAILURE;
        }
        else if (file.getType() == FileType.FOLDER) {
            io.error("File is a directory: {}", file.getName());
            return Result.FAILURE;
        }
        else if (!file.isReadable()) {
            io.error("File is not readble: {}", file.getName());
            return Result.FAILURE;
        }
        else if (!file.isWriteable()) {
            io.error("File is not writable: {}", file.getName());
            return Result.FAILURE;
        }
        FileObject tmp = file;

        FileSystem fs = file.getFileSystem();
        log.debug("File system: {}", fs);

        // If the file is not on the local file system, then create tmp file for editing
        if (!(fs instanceof LocalFileSystem)) {
            // Create a new temporary file, copy the contents for editing
            tmp = resolveFile(context, "tmp:/gshell.edit-" + System.currentTimeMillis() + ".txt");
            log.debug("Using temporary file for edit: {} ({})", tmp, tmp.getClass());
            tmp.createFile();
            tmp.copyFrom(file, Selectors.SELECT_SELF);
        }

        // Have to dereference the VFS file into a local file so the editor can access it
        File localFile = getLocalFile(tmp);
        Object result = edit(context, localFile);

        // If we had to use a tmp file for editing, then copy back and clean up
        if (tmp != file) {
            log.debug("Updating original file with edited content");
            file.copyFrom(tmp, Selectors.SELECT_SELF);
            tmp.delete();
            closeFile(tmp);
        }

        closeFile(file);
        
        return result;
    }

    private File getLocalFile(final FileObject file) throws Exception {
        assert file != null;
        assert file instanceof LocalFile;

        // This uses our custom accessible LocalFile implementation, which allows us to grap the File object.
        LocalFile lfile = (LocalFile)file;

        // Force the file to attach if it hasn't already
        lfile.refresh();

        return lfile.getLocalFile();
    }

    private Object edit(final CommandContext context, final File localFile) throws Exception {
        assert context != null;
        assert localFile != null;

        log.debug("Editing file: {}", localFile);

        List<String> editorCmd = selectEditor();

        log.debug("Executing: {} {}", editorCmd, localFile);

        ProcessBuilder builder = new ProcessBuilder();
        for (String s : editorCmd) {
            builder.command().add(s);
        }
        builder.command().add(localFile.getAbsolutePath());

        Process p = builder.start();

        log.debug("Waiting for process to exit...");
        int status = p.waitFor();
        log.info("Process exited w/status: {}", status);

        return status;
    }

    private List<String> selectEditor() {
        String cmd;

        if (editor != null) {
            cmd = editor;
        }
        else {
            // TODO: Expose a configurable preference for this
            cmd = getDefaultEditor();
        }

        // The editor configuration may need to set arguments or whatever, so we have to return a list
        return Arrays.asList(cmd.split("\\s"));
    }

    private String getDefaultEditor() {
        if (Os.isFamily(Os.OS_FAMILY_WINDOWS)) {
            return "NOTEPAD";    
        }
        else if (Os.isFamily(Os.OS_FAMILY_UNIX)) {
            if (System.getenv("DISPLAY") != null) {
                String tmp = System.getenv("XEDITOR");
                if (tmp != null) {
                    return tmp;
                }
            }

            String tmp = System.getenv("EDITOR");
            if (tmp != null) {
                return tmp;
            }

        }

        throw new RuntimeException("Unable to determine the default editor command");
    }
}