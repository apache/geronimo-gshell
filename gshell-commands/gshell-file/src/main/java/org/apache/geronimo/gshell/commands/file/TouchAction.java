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

package org.apache.geronimo.gshell.commands.file;

import org.apache.commons.vfs.FileObject;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.vfs.FileObjects;
import org.apache.geronimo.gshell.vfs.support.VfsActionSupport;

/**
 * Sets the last-modified time of a file.
 *
 * @version $Rev$ $Date$
 */
public class TouchAction
    extends VfsActionSupport
{
    @Argument(required=true)
    private String path;

    // TODO: Add options similar to UNIX touch (like -r FILE) see man page for more details.

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;

        FileObject file = resolveFile(context, path);

        try {
            if (!file.exists()) {
                file.createFile();
            }

            file.getContent().setLastModifiedTime(System.currentTimeMillis());
        }
        finally {
            FileObjects.close(file);
        }

        return CommandAction.Result.SUCCESS;
    }
}