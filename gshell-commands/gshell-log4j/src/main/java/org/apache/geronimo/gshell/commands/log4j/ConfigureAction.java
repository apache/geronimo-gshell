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
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.vfs.FileObjects;
import org.apache.geronimo.gshell.vfs.FileSystemAccess;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configure logging.
 *
 * @version $Rev$ $Date$
 */
public class ConfigureAction
    implements CommandAction
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final FileSystemAccess fileSystemAccess;

    @Argument
    private String path;

    public ConfigureAction(final FileSystemAccess fileSystemAccess) {
        assert fileSystemAccess != null;
        this.fileSystemAccess = fileSystemAccess;
    }

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        FileObject cwd = fileSystemAccess.getCurrentDirectory();
        FileObject file = fileSystemAccess.resolveFile(cwd, path);

        // TODO: Validate

        FileName name = file.getName();
        if (name.getExtension().equals("properties")) {
            PropertyConfigurator.configure(file.getURL());
        }
        else if (name.getExtension().equals("xml")) {
            DOMConfigurator.configure(file.getURL());
        }
        else {
            io.error("Do no know how to handle configuration file: {}", path);
            return Result.FAILURE;
        }

        FileObjects.close(file);

        return Result.SUCCESS;
    }
}