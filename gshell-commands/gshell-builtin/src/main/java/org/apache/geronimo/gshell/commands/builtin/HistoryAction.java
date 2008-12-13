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

package org.apache.geronimo.gshell.commands.builtin;

import jline.History;
import org.apache.geronimo.gshell.ansi.AnsiCode;
import org.apache.geronimo.gshell.ansi.AnsiRenderer;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;

import java.util.List;

/**
 * Display history.
 *
 * @version $Rev$ $Date$
 */
public class HistoryAction
    implements CommandAction
{
    // TODO: Support displaying a range of history
    
    // TODO: Add clear and recall support

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        // HACK: Get at the shell's history from our variables
        History history = context.getVariables().get("gshell.internal.history", History.class);
        assert history != null;

        // noinspection unchecked
        List<String> elements = history.getHistoryList();

        int i = 0;
        for (String element : elements) {
            String index = String.format("%3d", i);
            io.info("  {}  {}", AnsiRenderer.encode(index, AnsiCode.BOLD), element);
            i++;
        }

        return Result.SUCCESS;
    }
}