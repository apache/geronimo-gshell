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

import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.log4j.NDC;

/**
 * Manage the {@link NDC} for the current thread.
 *
 * @version $Rev$ $Date$
 */
public class NdcAction
    implements CommandAction
{
    private enum Type
    {
        PUSH,
        POP,
        PEEK
    }

    @Argument(index=0, required=true)
    private Type operation;

    @Argument(index=1)
    private String arg;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        switch (operation) {
            case PUSH:
                if (arg == null) {
                    throw new RuntimeException("Missing required argument");
                }
                NDC.push(arg);
                break;

            case POP:
                io.info("{}", NDC.pop());
                break;

            case PEEK:
                io.info("{}", NDC.peek());
                break;
        }

        return Result.SUCCESS;
    }
}