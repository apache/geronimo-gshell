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

import java.util.UUID;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.CommandExecutor;
import org.apache.geronimo.gshell.command.CommandNotFoundException;
import org.apache.geronimo.gshell.command.IO;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.command.descriptor.CommandDescriptor;
import org.apache.geronimo.gshell.common.Arguments;
import org.apache.geronimo.gshell.common.StopWatch;
import org.apache.geronimo.gshell.layout.LayoutManager;
import org.apache.geronimo.gshell.shell.Environment;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
@Component(role=CommandExecutor.class, hint="default")
public class DefaultCommandExecutor
    implements CommandExecutor, Initializable
{
    private Logger log = LoggerFactory.getLogger(getClass());

    public DefaultCommandExecutor() {}

    @Requirement
    private PlexusContainer container;

    @Requirement
    private LayoutManager layoutManager;

    @Requirement
    private CommandLineBuilder commandLineBuilder;

    public void initialize() throws InitializationException {
        /*
        assert evaluator != null;

        System.err.println("ENV: " + env);
        
        commandLineBuilder = new CommandLineBuilder(this, env, evaluator);
        */
    }

    public Object execute(final String line) throws Exception {
        assert line != null;

        log.info("Executing (String): {}", line);

        try {
            CommandLine cl = commandLineBuilder.create(line);

            return cl.execute();
        }
        catch (ErrorNotification n) {
            // Decode the error notifiation
            Throwable cause = n.getCause();

            if (cause instanceof Exception) {
                throw (Exception)cause;
            }
            else if (cause instanceof Error) {
                throw (Error)cause;
            }
            else {
                // Um, if we get this far, which we probably never will, then just re-toss the notifcation
                throw n;
            }
        }
    }

    public Object execute(final Object... args) throws Exception {
        assert args != null;
        assert args.length > 1;

        log.info("Executing (Object...): [{}]", Arguments.asString(args));

        return execute(String.valueOf(args[0]), Arguments.shift(args));
    }

    public Object execute(final String path, final Object[] args) throws Exception {
        assert path != null;
        assert args != null;

        log.info("Executing ({}): [{}]", path, Arguments.asString(args));

        // Look up the command descriptor for the given path
        final CommandDescriptor desc = layoutManager.find(path);
        if (desc == null) {
            throw new CommandNotFoundException(path);
        }

        // Create a new child container for the invocation and lookup the command instance
        String realmId = "gshell:" + UUID.randomUUID();

        log.debug("Child container realm: {}", realmId);

        final PlexusContainer childContainer = container.createChildContainer(realmId, container.getContainerRealm());
        final Command command = (Command)childContainer.lookup(desc.getRole(), desc.getRoleHint());

        //
        // NOTE: For now, until we can figure out a better way to have the container deal with this stuff, pass in
        //       the execution context manually
        //
        
        final Environment env = (Environment) container.lookup(Environment.class);

        // Setup the command context and pass it to the command instance
        CommandContext context = new CommandContext() {
            final Variables vars = new VariablesImpl(env.getVariables());

            public IO getIO() {
                return env.getIO();
            }

            public Variables getVariables() {
                return vars;
            }

            public CommandDescriptor getCommandDescriptor() {
                return desc;
            }
        };
        command.init(context);

        // Setup command timings
        StopWatch watch = new StopWatch(true);

        Object result;
        try {
            result = command.execute(args);

            log.debug("Command completed with result: {}, after: {}", result, watch);
        }
        finally {
            // Make sure that the commands output has been flushed
            try {
                env.getIO().flush();
            }
            catch (Exception ignore) {}

            // Nuke the child container
            container.removeChildContainer(realmId);
        }

        return result;
    }
}