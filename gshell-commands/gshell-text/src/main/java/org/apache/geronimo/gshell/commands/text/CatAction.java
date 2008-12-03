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

package org.apache.geronimo.gshell.commands.text;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileContentInfo;
import org.apache.commons.vfs.FileObject;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.io.Closer;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.vfs.FileObjects;
import org.apache.geronimo.gshell.vfs.support.VfsActionSupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Displays the contents of a file.
 *
 * @version $Rev$ $Date$
 */
public class CatAction
    extends VfsActionSupport
{
    @Option(name="-n")
    private boolean displayLineNumbers;

    @Argument(required=true)
    private String path;
    
    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        //
        // TODO: Support multi-path cat, and the special '-' token (which is the default if no paths are given)
        //

        FileObject file = resolveFile(context, path);

        ensureFileExists(file);
        ensureFileHasContent(file);

        FileContent content = file.getContent();
        FileContentInfo info = content.getContentInfo();
        log.debug("Content type: {}", info.getContentType());
        log.debug("Content encoding: {}", info.getContentEncoding());

        //
        // TODO: Only cat files which we think are text, or warn if its not, allow flag to force
        //

        log.debug("Displaying file: {}", file.getName());

        BufferedReader reader = new BufferedReader(new InputStreamReader(content.getInputStream()));
        try {
            cat(reader, io);
        }
        finally {
            Closer.close(reader);
        }

        FileObjects.close(file);
        
        return CommandAction.Result.SUCCESS;
    }

    private void cat(final BufferedReader reader, final IO io) throws IOException {
        String line;
        int lineno = 1;

        while ((line = reader.readLine()) != null) {
            if (displayLineNumbers) {
                io.out.print(String.format("%6d  ", lineno++));
            }
            io.out.println(line);
        }
    }
}