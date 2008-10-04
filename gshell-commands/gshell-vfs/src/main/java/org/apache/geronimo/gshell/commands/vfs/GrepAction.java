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

import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.oro.text.MatchAction;
import org.apache.oro.text.MatchActionProcessor;
import org.apache.oro.text.MatchActionInfo;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.commons.vfs.FileObject;
import org.codehaus.plexus.util.IOUtil;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;

/**
 * Displays lines matching a pattern.
 *
 * @version $Rev$ $Date$
 */
public class GrepAction
    extends VfsActionSupport
{
    @Argument(index=0, required=true)
    private String pattern;

    @Argument(index=1, required=true)
    private String path;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        final IO io = context.getIo();

        MatchActionProcessor processor = new MatchActionProcessor();

        try {
            processor.addAction(pattern, new MatchAction() {
                public void processMatch(final MatchActionInfo info) {
                    io.info("{}", info.line);
                }
            });
        }
        catch (MalformedPatternException e) {
            io.error("Invalid pattern: " + e, e);
            return Result.FAILURE;
        }

        FileObject file = resolveFile(context, path);
        BufferedInputStream input = new BufferedInputStream(file.getContent().getInputStream());
        try {
            processor.processMatches(input, io.outputStream);
        }
        finally {
            IOUtil.close(input);
            closeFile(file);
        }

        return Result.SUCCESS;
    }
}
