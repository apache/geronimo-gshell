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

package org.apache.geronimo.gshell.commands.optional;

import java.util.List;

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.commands.optional.util.PumpStreamHandler;
import org.apache.geronimo.gshell.console.IO;

/**
 * Execute system processes.
 *
 * @version $Rev$ $Date$
 */
public class ExecuteCommand
    extends CommandSupport
{
    private ProcessBuilder builder;

    @Argument(description="Argument", required=true)
    private List<String> args;

    public ExecuteCommand() {
        super("exec");
    }

    protected String getUsage() {
        return super.getUsage() + " <command> (<arg>)*";
    }

    protected Object doExecute() throws Exception {
        assert builder != null;

        boolean info = log.isInfoEnabled();

        if (info) {
            log.info("Executing: " + builder.command());
        }

        IO io = getIO();

        //
        // TODO: May need to expose the Process's destroy() if Command abort() is issued?
        //

        Process p = builder.start();

        PumpStreamHandler handler = new PumpStreamHandler(io);
        handler.attach(p);
        handler.start();

        log.debug("Waiting for process to exit...");

        int status = p.waitFor();

        if (info) {
            log.info("Process exited w/status: " + status);
        }

        handler.stop();

        return status;
    }
}
