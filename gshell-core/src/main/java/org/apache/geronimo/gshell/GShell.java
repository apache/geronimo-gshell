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

package org.apache.geronimo.gshell;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.time.StopWatch;
import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.command.CommandExecutor;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandManager;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.command.VariablesMap;
import org.apache.geronimo.gshell.command.CommandException;
import org.apache.geronimo.gshell.commandline.CommandLineBuilder;
import org.apache.geronimo.gshell.commandline.CommandLine;
import org.apache.geronimo.gshell.util.Arguments;

/**
 * ???
 *
 * @version $Id$
 */
public class GShell
    implements CommandExecutor
{
    private static final Log log = LogFactory.getLog(GShell.class);

    private final IO io;

    private final CommandManager commandManager;

    public GShell(final IO io) throws CommandException {
        if (io == null) {
            throw new IllegalArgumentException("IO is null");
        }

        this.io = io;
        
        //
        // HACK: DI CommandManager...  Maybe need to setup the top-level container here
        //

        this.commandManager = new CommandManager();
    }
    
    public GShell() throws CommandException {
        this(new IO());
    }

    public int execute(final String commandLine) throws Exception {
        assert commandLine != null;

        log.info("Executing (String): " + commandLine);

        //
        // HACK: Just to get something to work...
        //

        CommandLineBuilder builder = new CommandLineBuilder(this);
        CommandLine cl = builder.create(commandLine);
        cl.execute();

        //
        // HACK: Current API needs to be revised to pass data back,
        //       will be fixed latger, ignore for now
        //

        return 0;
    }

    public int execute(final String commandName, String[] args) throws Exception {
        assert commandName != null;
        assert args != null;

        boolean debug = log.isDebugEnabled();

        log.info("Executing (" + commandName + "): " + Arguments.asString(args));

        //
        // HACK: Just get something working right now
        //

        Command cmd = commandManager.getCommand(commandName);

        cmd.init(new CommandContext() {
            Variables vars = new VariablesMap();

            public IO getIO() {
                return io;
            }

            public Variables getVariables() {
                return vars;
            }
        });

        // Setup command timings
        StopWatch watch = null;
        if (debug) {
            watch = new StopWatch();
            watch.start();
        }

        int status;

        try {
            status = cmd.execute(args);

            if (debug) {
                log.debug("Command completed in " + watch);
            }
        }
        finally {
            cmd.destroy();
        }

        return status;
    }

    public int execute(final String... args) throws Exception {
        assert args != null;
        assert args.length > 1;

        log.info("Executing (String[]): " + Arguments.asString(args));

        return execute(args[0], Arguments.shift(args));
    }
}
