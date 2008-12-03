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

package org.apache.geronimo.gshell.commands.shell;

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.io.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Execute system processes.
 *
 * @version $Rev$ $Date$
 */
public class ExecuteAction
    implements CommandAction
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Argument(required=true)
    private List<String> args;

    // TODO: Support setting the process directory and envrionment muck

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        ProcessBuilder builder = new ProcessBuilder(args);

        log.info("Executing: {}", builder.command());

        Process p = builder.start();

        PumpStreamHandler handler = new PumpStreamHandler(io.inputStream, io.outputStream, io.errorStream);
        handler.attach(p);
        handler.start();

        log.debug("Waiting for process to exit...");

        int status = p.waitFor();
        
        log.info("Process exited w/status: {}", status);

        handler.stop();

        return status;
    }
}
