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
import org.apache.commons.vfs.Selectors;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.vfs.FileObjects;
import org.apache.geronimo.gshell.vfs.support.VfsActionSupport;

/**
 * Copies a file or directory.
 *
 * @version $Rev$ $Date$
 */
public class CopyAction
    extends VfsActionSupport
{
    @Argument(index=0, required=true)
    private String sourcePath;

    @Argument(index=1, required=true)
    private String targetPath;

    // TODO: Add --recursive suport

    // TODO: Add --verbose support
    
    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        FileObject source = resolveFile(context, sourcePath);
        FileObject target = resolveFile(context, targetPath);

        ensureFileExists(source);

        // TODO: Validate more

        if (target.exists() && target.getType().hasChildren()) {
            target = target.resolveFile(source.getName().getBaseName());
        }

        log.info("Copying {} -> {}", source, target);

        target.copyFrom(source, Selectors.SELECT_ALL);

        FileObjects.closeAll(source, target);
        
        return Result.SUCCESS;
    }
}