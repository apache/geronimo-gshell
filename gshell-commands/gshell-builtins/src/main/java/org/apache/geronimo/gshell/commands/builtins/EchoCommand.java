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

package org.apache.geronimo.gshell.commands.builtins;

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A simple command to <em>echo</em> all given arguments to the commands standard output.
 *
 * @version $Rev$ $Date$
 */
public class EchoCommand
    implements CommandAction
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Option(name="-n", description="Do not print the trailing newline character")
    private boolean trailingNewline = true;

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    @Argument(description="Arguments")
    private List<String> args;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;

        IO io = context.getIo();

        if (args != null) {
            int c=0;

            for (String arg : args) {
                io.out.print(arg);
                if (++c + 1 < args.size()) {
                    io.out.print(" ");
                }
            }
        }

        if (trailingNewline) {
            io.out.println();
        }

        return Result.SUCCESS;
    }
}