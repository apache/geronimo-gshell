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

package org.apache.geronimo.gshell.commands.fileutils;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.Selectors;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.vfs.FileObjects;
import org.apache.geronimo.gshell.vfs.support.VfsActionSupport;

/**
 * Remove a file or directory.
 *
 * @version $Rev$ $Date$
 */
public class RemoveAction
    extends VfsActionSupport
{
    @Argument(required=true)
    private String path;

    // TODO: Add --recursive support

    // TODO: Add --verbose support

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        FileObject file = resolveFile(context, path);

        ensureFileExists(file);

        try {
            file.delete(Selectors.SELECT_SELF);
        }
        finally {
            FileObjects.close(file);
        }

        return Result.SUCCESS;
    }
}