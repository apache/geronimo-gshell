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

package org.apache.geronimo.gshell.builtins;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.MessageSource;
import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.ExitNotification;
import org.apache.geronimo.gshell.util.Arguments;

/**
 * Exit the current shell.
 *
 * @version $Id$
 */
public class ExitCommand
    extends CommandSupport
{
    public ExitCommand() {
        super("exit");
    }

    protected String getUsage() {
        return super.getUsage() + " [code]";
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
        int exitCode = 0;

        if (args.length > 1) {
            io.err.println("Unexpected arguments: " + Arguments.asString(args));
            io.err.println();
            usage = true;
        }
        if (args.length == 1) {
            exitCode = Integer.parseInt(args[0]);
        }

        if (usage || line.hasOption('h')) {
            displayHelp(options);

            return Command.SUCCESS;
        }

        exit(exitCode);

        // Should never get this far
        assert false;

        return Command.FAILURE;
    }

    private void exit(final int exitCode) {
        log.info("Exiting w/code: " + exitCode);

        //
        // DO NOT Call System.exit() !!!
        //

        throw new ExitNotification(exitCode);
    }
}
