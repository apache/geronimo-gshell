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

package org.apache.geronimo.gshell.rapture;

import org.apache.geronimo.gshell.clp.CommandLineProcessor;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.clp.Printer;
import org.apache.geronimo.gshell.clp.ProcessingException;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContainer;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.CommandInfo;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.plexus.GShellPlexusContainer;
import org.apache.geronimo.gshell.util.Arguments;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default {@link CommandContainer} component.
 *
 * @version $Rev$ $Date$
 */
@Component(role=CommandContainer.class)
public class DefaultCommandContainer
    implements CommandContainer, Contextualizable
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private GShellPlexusContainer container;

    @Configuration("") // Just to mark what this is used for, since we have to configure a default value
    private String commandId;

    // Contextualizable
    
    public void contextualize(final Context context) throws ContextException {
        assert context != null;

        container = (GShellPlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
        assert container != null;
        
        log.debug("Container: {}", container);
    }

    private CommandAction getAction() {
        assert container != null;

        try {
            return container.lookupComponent(CommandAction.class, commandId);
        }
        catch (ComponentLookupException e) {
            throw new RuntimeException(e);
        }
    }

    // CommandContainer

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;

        log.trace("Executing; context={}");

        CommandAction action = getAction();

        // TODO: Handle logging muck
        // NOTE: For logging, just set the NDC/MDC and let the loggers name be whatever the command set it to be.
        
        // TODO: Bind context, io and variables
        // NOTE: No, no no...

        // Process command line options/arguments, return if we have been asked to display --help
        if (processArguments(context, action, context.getArguments())) {
            return CommandAction.Result.SUCCESS;
        }

        Object result = action.execute(context);

        log.trace("Result: {}", result);

        return result;
    }

    private boolean processArguments(final CommandContext context, final CommandAction action, final Object[] args) throws ProcessingException {
        assert context != null;
        assert args != null;

        CommandLineProcessor clp = new CommandLineProcessor();
        clp.addBean(action);

        // Attach some help context
        HelpSupport help = new HelpSupport();
        clp.addBean(help);

        // Process the arguments
        clp.process(Arguments.toStringArray(args));

        // Display help if option detected
        if (help.displayHelp) {
            help.display(context, clp);
            return true;
        }

        return false;
    }

    private static class HelpSupport
    {
        @Option(name="-h", aliases={"--help"}, description="Display this help message", requireOverride=true)
        public boolean displayHelp;

        protected void display(final CommandContext context, final CommandLineProcessor clp) {
            assert context != null;
            assert clp != null;

            // Use the alias if we have one, else use the command name
            CommandInfo info = context.getInfo();
            String name = info.getAlias();
            if (name == null) {
                name = info.getName();
            }

            IO io = context.getIo();
            Printer printer = new Printer(clp);
            printer.printUsage(io.out, name);
            io.out.println();
        }
    }
}