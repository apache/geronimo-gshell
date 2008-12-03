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

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;
import org.apache.log4j.MDC;

import java.util.Map;

/**
 * Manage the {@link MDC} for the current thread.
 *
 * @version $Rev$ $Date$
 */
public class MdcAction
    implements CommandAction
{
    @Option(name="-r", aliases={"--remove"})
    private boolean remove;

    @Argument(index=0)
    private String name;

    @Argument(index=1)
    private Object value;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        if (remove) {
            MDC.remove(name);
        }
        else if (name != null && value != null) {
            MDC.put(name, value);
        }
        else if (name != null) {
            io.info("{}", MDC.get(name));
        }
        else {
            Map map = MDC.getContext();
            if (map != null) {
                for (Object key : map.keySet()) {
                    io.info("{}={}", key, map.get(key));
                }
            }
        }

        return Result.SUCCESS;
    }
}