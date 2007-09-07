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

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.IO;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.command.descriptor.CommandDescriptor;
import org.apache.geronimo.gshell.common.Arguments;
import org.apache.geronimo.gshell.common.StopWatch;
import org.apache.geronimo.gshell.layout.LayoutManager;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.evaluator.ExpressionEvaluator;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the primary implementation of {@link Shell}.
 *
 * @version $Rev$ $Date$
 */
@Component(role=Shell.class)
public class ShellImpl
    implements Shell, Initializable
{
    private Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private IO io;

    @Requirement
    private PlexusContainer container;

    @Requirement
    private LayoutManager layoutManager;

    @Requirement
    private ExpressionEvaluator evaluator;

    private CommandLineBuilder commandLineBuilder;

    private Variables variables = new VariablesImpl();

    public void initialize() throws InitializationException {
        assert evaluator != null;
        commandLineBuilder = new CommandLineBuilder(this, evaluator);
    }

    public Variables getVariables() {
        return variables;
    }

    public IO getIO() {
        return io;
    }

    public Object execute(final String commandLine) throws Exception {
        assert commandLine != null;

        log.info("Executing (String): {}", commandLine);

        CommandLine cl = commandLineBuilder.create(commandLine);
        return cl.execute();
    }

    public Object execute(final String commandName, final Object[] args) throws Exception {
        assert commandName != null;
        assert args != null;

        log.info("Executing ({}): {}", commandName, Arguments.asString(args));

        //
        // HACK: Probably need to pick a better way to name the command invocation container, or do we even really need this?
        //

        String realmId = "command-invocation";

        final PlexusContainer childContainer = container.createChildContainer(realmId, container.getContainerRealm());
        final CommandDescriptor commandDescriptor = (CommandDescriptor) childContainer.getComponentDescriptor(Command.class.getName(), commandName);
        final Command command = (Command)childContainer.lookup(Command.class, commandName);

        //
        // NOTE: For now, until we can figure out a better way to have the container deal with this stuff, pass in
        //       the execution context manually
        //
        
        CommandContext context = new CommandContext() {
            final Variables vars = new VariablesImpl(ShellImpl.this.getVariables());

            public IO getIO() {
                return io;
            }

            public Variables getVariables() {
                return vars;
            }

            public CommandDescriptor getCommandDescriptor() {
                return commandDescriptor;
            }
        };

        command.init(context);

        // Setup command timings
        StopWatch watch = new StopWatch(true);

        Object result;
        try {
            result = command.execute(args);

            log.debug("Command completed in: {}", watch);
        }
        finally {
            // Make sure that the commands output has been flushed
            try {
                io.flush();
            }
            catch (Exception ignore) {}

            //
            // HACK: Nuke the child container now
            //

            container.removeChildContainer(realmId);
        }

        return result;
    }

    public Object execute(final Object... args) throws Exception {
        assert args != null;
        assert args.length > 1;

        log.info("Executing (Object...): {}", Arguments.asString(args));

        return execute(String.valueOf(args[0]), Arguments.shift(args));
    }
}