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

package org.apache.geronimo.gshell.vfs;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.geronimo.gshell.application.ApplicationManager;
import org.apache.geronimo.gshell.command.Variables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * {@link FileSystemAccess} component.
 *
 * @version $Rev$ $Date$
 */
public class FileSystemAccessImpl
    implements FileSystemAccess
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationManager applicationManager;

    @Autowired
    private FileSystemManager fileSystemManager;

    public FileSystemManager getManager() {
        assert fileSystemManager != null;
        return fileSystemManager;
    }

    public FileObject getCurrentDirectory(final Variables vars) throws FileSystemException {
        assert vars != null;

        FileObject cwd = null;

        Object var = vars.get(CWD);
        if (var instanceof String) {
            log.trace("Resolving CWD from string: {}", var);

            cwd = getManager().resolveFile((String)var);
        }
        else if (var instanceof FileObject) {
            cwd = (FileObject)var;
        }
        else if (var != null) {
            throw new RuntimeException("Invalid variable type for '" + CWD + "'; expected String or FileObject; found: " + var.getClass().getName());
        }

        if (cwd == null) {
            log.trace("CWD not set, resolving from user.dir");

            // TODO: May need to ask the Application for this, as it might be different depending on the context (ie. remote user, etc)
            String userDir = System.getProperty("user.dir");
            cwd = getManager().resolveFile(userDir);
        }

        return cwd;
    }

    public FileObject getCurrentDirectory() throws FileSystemException {
        assert applicationManager != null;

        log.trace("Resolving CWD from application variables");

        return getCurrentDirectory(applicationManager.getApplication().getVariables());
    }

    public void setCurrentDirectory(final Variables vars, final FileObject dir) throws FileSystemException {
        assert vars != null;
        assert dir != null;

        log.trace("Setting CWD: {}", dir);

        // Make sure that the given file object exists and is really a directory
        if (!dir.exists()) {
            throw new RuntimeException("Directory not found: " + dir.getName());
        }
        else if (dir.getType() != FileType.FOLDER) {
            throw new RuntimeException("File is not a directory: " + dir.getName());
        }

        vars.parent().set(CWD, dir);
    }

    public void setCurrentDirectory(final FileObject dir) throws FileSystemException {
        assert dir != null;

        assert applicationManager != null;

        log.trace("Setting CWD to application variables");

        setCurrentDirectory(applicationManager.getApplication().getVariables(), dir);
    }

    public FileObject resolveFile(final FileObject baseFile, final String name) throws FileSystemException {
        return getManager().resolveFile(baseFile, name);
    }

    public FileObject resolveFile(final String name) throws FileSystemException {
        return getManager().resolveFile(getCurrentDirectory(), name);
    }
}