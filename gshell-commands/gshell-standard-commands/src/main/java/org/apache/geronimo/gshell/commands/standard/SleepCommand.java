/*
 * Copyright 2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.gshell.commands.standard;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.MessageSource;
import org.apache.geronimo.gshell.console.IO;

/**
 * Sleep... zzzZ
 *
 * @version $Id$
 */
public class SleepCommand
    extends CommandSupport
{
    public SleepCommand() {
        super("sleep");
    }

    protected String getUsage() {
        return super.getUsage() + " <time>";
    }

    protected int doExecute(String[] args) throws Exception {
        assert args != null;

        MessageSource messages = getMessageSource();

        IO io = getIO();

        Options options = getOptions();

        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, args);
        args = line.getArgs();

        boolean usage = false;
        long time = -1;

        if (args.length != 1) {
            usage = true;
        }
        else {
            time = Long.parseLong(args[0]);
        }

        if (usage || line.hasOption('h')) {
            displayHelp(options);

            return Command.SUCCESS;
        }

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
