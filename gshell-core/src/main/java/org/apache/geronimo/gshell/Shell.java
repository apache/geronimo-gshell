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

package org.apache.geronimo.gshell;

import java.util.Iterator;

import org.apache.commons.lang.time.StopWatch;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.CommandDefinition;
import org.apache.geronimo.gshell.command.CommandException;
import org.apache.geronimo.gshell.command.CommandManager;
import org.apache.geronimo.gshell.command.CommandManagerImpl;
import org.apache.geronimo.gshell.command.MessageSource;
import org.apache.geronimo.gshell.command.MessageSourceImpl;
import org.apache.geronimo.gshell.command.StandardVariables;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.command.VariablesImpl;
import org.apache.geronimo.gshell.commandline.CommandLine;
import org.apache.geronimo.gshell.commandline.CommandLineBuilder;
import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.util.Arguments;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the primary interface to executing named commands.
 *
 * @version $Rev$ $Date$
 */
public class Shell
{
    //
    // TODO: Introduce Shell interface?
    //

    private static final Logger log = LoggerFactory.getLogger(Shell.class);

    private final IO io;

    private final ShellContainer shellContainer = new ShellContainer();

    private final CommandManager commandManager;

    private final CommandLineBuilder commandLineBuilder;

    private final Variables variables = new VariablesImpl();

    public Shell(final IO io) throws CommandException {
        assert io != null;
        
        this.io = io;

        /*
        ContainerConfiguration config = new DefaultContainerConfiguration();

        config.setName("gshell.core");
        config.setClassWorld(new ClassWorld("gshell.core", Thread.currentThread().getContextClassLoader()));

        try {
            PlexusContainer plexus = new DefaultPlexusContainer(config);
            
            System.err.println("Booted plexus container: " + plexus);

            plexus.lookup("fuckfuckfuck");
        }
        catch (Exception e) {
            throw new CommandException(e);
        }
        */
        
        shellContainer.registerComponentInstance(this);
        shellContainer.registerComponentImplementation(CommandManagerImpl.class);
        shellContainer.registerComponentImplementation(CommandLineBuilder.class);

        //
        // TODO: Refactor to use the container, now that we have one
        //

        this.commandManager = (CommandManager) shellContainer.getComponentInstanceOfType(CommandManager.class);
        this.commandLineBuilder = (CommandLineBuilder) shellContainer.getComponentInstanceOfType(CommandLineBuilder.class);

        //
        // HACK: Set some default variables
        //

        variables.set(StandardVariables.PROMPT, "> ");
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

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public Object execute(final String commandLine) throws Exception {
        assert commandLine != null;

        if (log.isInfoEnabled()) {
            log.info("Executing (String): " + commandLine);
        }

        CommandLine cl = commandLineBuilder.create(commandLine);
        return cl.execute();
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

    public Object execute(final String commandName, final Object[] args) throws Exception {
        assert commandName != null;
        assert args != null;

        boolean debug = log.isDebugEnabled();

        if (log.isInfoEnabled()) {
            log.info("Executing (" + commandName + "): " + Arguments.asString(args));
        }

        // Setup the command container
        ShellContainer container = new ShellContainer(shellContainer);

        final CommandDefinition def = commandManager.getCommandDefinition(commandName);
        final Class type = def.loadClass();

        //
        // TODO: Pass the command instance the name it was registered with?, could be an alias
        //

        container.registerComponentInstance(def);
        container.registerComponentImplementation(type);

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

            MessageSource messageSource;

            public MessageSource getMessageSource() {
                // Lazy init the messages, commands many not need them
                if (messageSource == null) {
                    messageSource = new MessageSourceImpl(type.getName() + "Messages");
                }

                return messageSource;
            }
        });

        // Setup command timings
        StopWatch watch = null;
        if (debug) {
            watch = new StopWatch();
            watch.start();
        }

        Object result;
        try {
            result = cmd.execute(args);

            if (debug) {
                log.debug("Command completed in " + watch);
            }
        }
        finally {
            cmd.destroy();

            shellContainer.removeChildContainer(container);
            // container.stop() container.dispose() ?
        }

        return result;
    }

    public Object execute(final Object... args) throws Exception {
        assert args != null;
        assert args.length > 1;

        if (log.isInfoEnabled()) {
            log.info("Executing (Object...): " + Arguments.asString(args));
        }

        return execute(String.valueOf(args[0]), Arguments.shift(args));
    }
}
