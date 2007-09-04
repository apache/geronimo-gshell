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

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.IO;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.common.Arguments;
import org.apache.geronimo.gshell.common.StopWatch;
import org.apache.geronimo.gshell.layout.LayoutManager;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the primary interface to executing named commands.
 *
 * @version $Rev$ $Date$
 */
@Component(role = Shell.class)
public class Shell
    implements Initializable
{
    private Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private IO io;

    @Requirement
    private PlexusContainer container;

    @Requirement
    private LayoutManager layoutManager;

    @Requirement
    private CommandLineBuilder commandLineBuilder;

    private Variables variables = new VariablesImpl();

    public Shell() {
        //
        // HACK: Set some default variables
        //

        variables.set("gshell.prompt", "> ");
    }

    //
    // HACK: This is for testing, need to weed out and refactor all this shiz
    //
    
    public Shell(final IO io) {
        this.io = io;
    }
    
    public Variables getVariables() {
        return variables;
    }

    public IO getIO() {
        return io;
    }

    public void initialize() throws InitializationException {
        // Dump some debug to crapski
        if (log.isDebugEnabled()) {
            log.debug("Container: {}", container);
            log.debug("Layout manager: {}", layoutManager);
            log.debug("Builder: {}", commandLineBuilder);
        }
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
            log.debug("    {}={}", name, vars.get(name));
        }
    }

    public Object execute(final String commandName, final Object[] args) throws Exception {
        assert commandName != null;
        assert args != null;

        boolean debug = log.isDebugEnabled();

        if (log.isInfoEnabled()) {
            log.info("Executing (" + commandName + "): " + Arguments.asString(args));
        }

        //
        // HACK: Probably need to pick a better way to name the command invocation container, or do we even really need this?
        //

        final PlexusContainer childContainer = container.createChildContainer("command-invocation", container.getContainerRealm());
        final Command command = (Command)childContainer.lookup(Command.class, commandName);
        final Variables vars = new VariablesImpl(getVariables());

        command.init(new CommandContext() {
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

        Object result;
        try {
            result = command.execute(args);

            if (debug) {
                log.debug("Command completed in " + watch);
            }
        }
        finally {
            command.destroy();

            //
            // HACK: Nuke the child container now
            //
            
            container.removeChildContainer("command-invocation");
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
