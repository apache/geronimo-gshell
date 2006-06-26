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
import org.apache.commons.cli.Options;

import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.CommandException;
import org.apache.geronimo.gshell.command.MessageSource;
import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.commands.standard.util.PumpStreamHandler;

/**
 * Execute system processes.
 *
 * @version $Id$
 */
public class ExecuteCommand
    extends CommandSupport
{
    private ProcessBuilder builder;

    public ExecuteCommand() {
        super("exec");
    }

    protected Options getOptions() {
        MessageSource messages = getMessageSource();

        Options options = super.getOptions();

//        options.addOption(OptionBuilder
//            .withDescription(messages.getMessage("cli.option.n"))
//            .create('n'));

        return options;
    }

    protected String getUsage() {
        return super.getUsage() + " <command> (<arg>)*";
    }

    protected boolean processCommandLine(final CommandLine line) throws CommandException {
        assert line != null;

        String[] args = line.getArgs();

        // Need at least one argument
        if (args.length < 1) {
            return true;
        }
        else {
            builder = new ProcessBuilder(args);
        }

        //
        // TODO: Allow ENV to be changed (default is given environ)
        //

        //
        // TODO: Allow working dir to be set (default is user.dir)
        //

        //
        // TODO: Allow error redirection to be enabled (default is false)
        //

        //
        // TODO: Add timeout; default is no timeout
        //

        //
        // TODO: Add spawn flag (process not killed when vm exits; default is not to spawn)
        //

        return false;
    }

    protected Object doExecute(final Object[] args) throws Exception {
        assert args != null;
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
