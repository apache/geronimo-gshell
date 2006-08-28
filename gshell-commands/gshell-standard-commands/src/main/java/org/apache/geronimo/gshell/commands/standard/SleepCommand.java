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

package org.apache.geronimo.gshell.commands.standard;

import org.apache.commons.cli.CommandLine;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.CommandException;

/**
 * Sleep... zzzZ
 *
 * @version $Rev$ $Date$
 */
public class SleepCommand
    extends CommandSupport
{
    private long time = -1;

    public SleepCommand() {
        super("sleep");
    }

    protected String getUsage() {
        return super.getUsage() + " <milliseconds>";
    }

    protected boolean processCommandLine(final CommandLine line) throws CommandException {
        assert line != null;

        String[] args = line.getArgs();

        if (args.length != 1) {
            return true;
        }
        else {
            time = Long.parseLong(args[0]);
        }

        return false;
    }

    protected Object doExecute(final Object[] args) throws Exception {
        assert args != null;

        log.info("Sleeping for " + time);

        try {
            Thread.sleep(time);
        }
        catch (InterruptedException ignore) {
            log.debug("Sleep was interrupted... :-(");
        }

        log.info("Awake now");

        return Command.SUCCESS;
    }
}
