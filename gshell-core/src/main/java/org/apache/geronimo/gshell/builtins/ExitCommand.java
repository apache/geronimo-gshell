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

package org.apache.geronimo.gshell.builtins;

import org.apache.geronimo.gshell.ExitNotification;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.CommandSupport;

/**
 * Exit the current shell.
 *
 * @version $Rev$ $Date$
 */
public class ExitCommand
    extends CommandSupport
{
    @Argument(description="System exit code")
    private int exitCode = 0;

    public ExitCommand() {
        super("exit");
    }

    protected String getUsage() {
        return super.getUsage() + " [code]";
    }

    protected Object doExecute() throws Exception {
        log.info("Exiting w/code: " + exitCode);

        //
        // DO NOT Call System.exit() !!!
        //

        throw new ExitNotification(exitCode);
    }
}
