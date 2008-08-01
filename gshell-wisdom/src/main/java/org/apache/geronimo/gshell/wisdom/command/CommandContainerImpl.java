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

package org.apache.geronimo.gshell.wisdom.command;

import org.apache.geronimo.gshell.clp.CommandLineProcessor;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.clp.ProcessingException;
import org.apache.geronimo.gshell.command.Arguments;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandCompleter;
import org.apache.geronimo.gshell.command.CommandContainer;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.CommandDocumenter;
import org.apache.geronimo.gshell.command.CommandResult;
import org.apache.geronimo.gshell.notification.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * The default {@link CommandContainer} component.
 *
 * @version $Rev$ $Date$
 */
public class CommandContainerImpl
    implements CommandContainer
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    // private GShellPlexusContainer container;

    private String commandId;

    // Command

    public String getId() {
        return commandId;
    }

    private <T> T lookupComponent(final Class<T> role) {
        /*
        assert role != null;
        assert container != null;

        try {
            return container.lookupComponent(role, commandId);
        }
        catch (ComponentLookupException e) {
            throw new RuntimeException(e);
        }
        */

        return null;
    }

    public CommandAction getAction() {
        return lookupComponent(CommandAction.class);
    }

    public CommandDocumenter getDocumenter() {
        return lookupComponent(CommandDocumenter.class);
    }

    public CommandCompleter getCompleter() {
        return lookupComponent(CommandCompleter.class);
    }

    public CommandResult execute(final CommandContext context) {
        assert context != null;

        log.trace("Executing; context={}");

        // Provide logging context for the command execution
        MDC.put("commandId", commandId);

        CommandResult result;

        try {
            CommandAction action = getAction();

            // Process command line options/arguments, return if we have been asked to display --help
            try {
                if (processArguments(context, action, context.getArguments())) {
                    return new CommandResult(CommandAction.Result.SUCCESS);
                }
            }
            catch (ProcessingException e) {
                new CommandResult(e);
            }

            try {
                final Object value = action.execute(context);
                log.trace("Result: {}", value);

                result = new CommandResult(value);
            }
            catch (final Notification n) {
                log.trace("Notified: {}, n");

                result = new CommandResult(n);
            }
            catch (final Throwable t) {
                log.trace("Caught: {}", t);

                result = new CommandResult(t);
            }
        }
        finally {
            MDC.remove("commandId");
        }

        return result;
    }

    /**
     * Process command-line arguments for the action.
     *
     * @param context   The command context.
     * @param action    The action being executed.
     * @param args      The arguments to the action.
     * @return          True if --help was detetected, else execute the action.
     *
     * @throws org.apache.geronimo.gshell.clp.ProcessingException  A failure occured while processing the command-line.
     */
    private boolean processArguments(final CommandContext context, final CommandAction action, final Object[] args) throws ProcessingException {
        assert context != null;
        assert args != null;

        CommandLineProcessor clp = new CommandLineProcessor();
        clp.addBean(action);

        // Attach some help context
        CommandDocumenter documenter = getDocumenter();
        HelpSupport help = new HelpSupport();
        clp.addBean(help);
        clp.addBean(documenter);

        // Process the arguments
        clp.process(Arguments.toStringArray(args));

        // Render command-line usage
        if (help.displayHelp) {
            documenter.renderUsage(context.getInfo(), context.getIo().out);
            return true;
        }

        return false;
    }

    /**
     * Helper to inject <tt>--help<tt> support.  Package access to allow DefaultCommandDocumentor access.
     */
    static class HelpSupport
    {
        @Option(name="-h", aliases={"--help"}, description="Display this help message", requireOverride=true)
        public boolean displayHelp;
    }
}