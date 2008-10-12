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

package org.apache.geronimo.gshell.commands.log4j;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.Selectors;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.vfs.FileObjects;
import org.apache.geronimo.gshell.vfs.FileSystemAccess;
import org.apache.geronimo.gshell.vfs.provider.local.LocalFile;
import org.apache.geronimo.gshell.vfs.provider.local.LocalFileSystem;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;


/**
 * Configure logging.
 *
 * @version $Rev$ $Date$
 */
public class ConfigureAction
    implements CommandAction
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private FileSystemAccess fileSystemAccess;

    @Argument
    private String path;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        FileObject cwd = fileSystemAccess.getCurrentDirectory();
        FileObject file = fileSystemAccess.resolveFile(cwd, path);

        // TODO: Validate

        FileObject tmp = file;

        // If the file is not on the local file system, then create tmp file for editing
        if (!(file.getFileSystem() instanceof LocalFileSystem)) {
            // Create a new temporary file, copy the contents for editing
            tmp = fileSystemAccess.resolveFile(null, "tmp:/gshell.log4j.config-" + System.currentTimeMillis() + ".txt");
            log.debug("Using temporary file for edit: {} ({})", tmp, tmp.getClass());
            tmp.createFile();
            tmp.copyFrom(file, Selectors.SELECT_SELF);
        }

        File configFile = getLocalFile(tmp);

        FileName name = tmp.getName();
        if (name.getExtension().equals("properties")) {
            PropertyConfigurator.configure(configFile.toURI().toURL());
        }
        else if (name.getExtension().equals("xml")) {
            DOMConfigurator.configure(configFile.toURI().toURL());
        }
        else {
            io.error("Don't know how to handle configuration file: {}", path);
            return Result.FAILURE;
        }

        if (tmp != file) {
            tmp.delete();
            FileObjects.close(tmp);
        }

        FileObjects.close(file);

        return Result.SUCCESS;
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
}