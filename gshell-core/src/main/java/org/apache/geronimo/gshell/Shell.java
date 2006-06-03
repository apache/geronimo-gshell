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
import org.apache.geronimo.gshell.command.VariablesImpl;
import org.apache.geronimo.gshell.command.CommandException;
import org.apache.geronimo.gshell.command.CommandDefinition;
import org.apache.geronimo.gshell.command.CommandManagerImpl;
import org.apache.geronimo.gshell.commandline.CommandLineBuilder;
import org.apache.geronimo.gshell.commandline.CommandLine;
import org.apache.geronimo.gshell.util.Arguments;

import java.util.Iterator;

/**
 * ???
 *
 * @version $Id$
 */
public class Shell
    implements CommandExecutor
{
    private static final Log log = LogFactory.getLog(Shell.class);

    private final IO io;

    private final ShellContainer shellContainer = new ShellContainer();

    private final CommandManager commandManager;

    private final CommandLineBuilder commandLineBuilder;

    private final Variables variables = new VariablesImpl();

    public Shell(final IO io) throws CommandException {
        if (io == null) {
            throw new IllegalArgumentException("IO is null");
        }

        this.io = io;

        shellContainer.registerComponentInstance(this);
        shellContainer.registerComponentImplementation(CommandManagerImpl.class);
        shellContainer.registerComponentImplementation(CommandLineBuilder.class);

        //
        // TODO: Refactor to use the container, now that we have one
        //

        this.commandManager = (CommandManager) shellContainer.getComponentInstanceOfType(CommandManager.class);
        this.commandLineBuilder = (CommandLineBuilder) shellContainer.getComponentInstanceOfType(CommandLineBuilder.class);
    }

    public Shell() throws CommandException {
        this(new IO());
    }

    public Variables getVariables() {
        return variables;
    }

    public IO getIO() {
        return io;
    }

    public int execute(final String commandLine) throws Exception {
        assert commandLine != null;

        log.info("Executing (String): " + commandLine);

        CommandLine cl = commandLineBuilder.create(commandLine);
        cl.execute();

        //
        // TODO: Fix API to allow CL to pass back data
        //

        return 0;
    }

    //
    // CommandExecutor
    //

    private void dump(final Variables vars) {
        Iterator<String> iter = vars.names();

        if (iter.hasNext()) {
            log.debug("Variables:");
        }

        while (iter.hasNext()) {
            String name = iter.next();

            log.debug("    " + name + "=" + vars.get(name));
        }
    }

    public int execute(final String commandName, final String[] args) throws Exception {
        assert commandName != null;
        assert args != null;

        boolean debug = log.isDebugEnabled();

        log.info("Executing (" + commandName + "): " + Arguments.asString(args));

        // Setup the command container
        ShellContainer container = new ShellContainer(shellContainer);

        CommandDefinition def = commandManager.getCommandDefinition(commandName);
        container.registerComponentInstance(def);
        container.registerComponentImplementation(def.loadClass());

        // container.start() ?

        Command cmd = (Command) container.getComponentInstanceOfType(Command.class);

        //
        // TODO: DI all bits if we can, then free up "context" to replace "category" as a term
        //

        final Variables vars = new VariablesImpl(getVariables());

        cmd.init(new CommandContext() {
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

            shellContainer.removeChildContainer(container);
            // container.stop() container.dispose() ?
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
