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
import org.apache.geronimo.gshell.command.CommandExecutor;
import org.apache.geronimo.gshell.command.IO;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.common.Arguments;
import org.apache.geronimo.gshell.common.StopWatch;
import org.apache.geronimo.gshell.layout.LayoutManager;
import org.apache.geronimo.gshell.layout.NotFoundException;
import org.apache.geronimo.gshell.layout.model.AliasNode;
import org.apache.geronimo.gshell.layout.model.CommandNode;
import org.apache.geronimo.gshell.layout.model.Node;
import org.apache.geronimo.gshell.registry.CommandRegistry;
import org.apache.geronimo.gshell.registry.NotRegisteredException;
import org.apache.geronimo.gshell.shell.Environment;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default command executor.
 *
 * @version $Rev$ $Date$
 */
@Component(role=CommandExecutor.class, hint="default")
public class DefaultCommandExecutor
    implements CommandExecutor
{
    private Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private LayoutManager layoutManager;

    @Requirement
    private CommandRegistry commandRegistry;

    @Requirement
    private CommandLineBuilder commandLineBuilder;

    @Requirement
    private Environment env;

    public DefaultCommandExecutor() {}
    
    public DefaultCommandExecutor(final LayoutManager layoutManager, final CommandLineBuilder commandLineBuilder, final Environment env) {
        this.layoutManager = layoutManager;
        this.commandLineBuilder = commandLineBuilder;
        this.env = env;
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

    private String findCommandId(final Node node) throws NotFoundException {
        assert node != null;

        if (node instanceof AliasNode) {
            AliasNode aliasNode = (AliasNode) node;
            String targetPath = aliasNode.getCommand();
            Node target = layoutManager.findNode(layoutManager.getLayout(), targetPath);

            return findCommandId(target);
        }
        else if (node instanceof CommandNode) {
            CommandNode commandNode = (CommandNode) node;

            return commandNode.getId();
        }

        throw new NotFoundException("Unable to get command id for: " + node);
    }

    private Command findCommand(final String path) throws NotFoundException {
        Node node = layoutManager.findNode(path);

        String id = findCommandId(node);

        try {
            return commandRegistry.lookup(id);
        }
        catch (NotRegisteredException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    public Object execute(final String path, final Object[] args) throws Exception {
        assert path != null;
        assert args != null;

        log.info("Executing ({}): [{}]", path, Arguments.asString(args));

        // Look up the command for the given path
        Command command = findCommand(path);

        // Setup the command context and pass it to the command instance
        CommandContext context = new CommandContext() {
            // Command instances get their own namespace with defaults from the current
            final Variables vars = new DefaultVariables(env.getVariables());

            public IO getIO() {
                return env.getIO();
            }

            public Variables getVariables() {
                return vars;
            }
        };

        // Setup command timings
        StopWatch watch = new StopWatch(true);

        Object result;
        try {
            result = command.execute(context, args);

            log.debug("Command completed with result: {}, after: {}", result, watch);
        }
        finally {
            // Make sure that the commands output has been flushed
            try {
                env.getIO().flush();
            }
            catch (Exception ignore) {}
        }

        return result;
    }
}