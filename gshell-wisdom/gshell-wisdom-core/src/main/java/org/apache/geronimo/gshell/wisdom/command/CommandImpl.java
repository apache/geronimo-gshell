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
import org.apache.geronimo.gshell.command.Arguments;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.CommandDocumenter;
import org.apache.geronimo.gshell.command.CommandResult;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.i18n.ResourceBundleMessageSource;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.notification.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.PostConstruct;

/**
 * The default {@link Command} component.
 *
 * @version $Rev$ $Date$
 */
public class CommandImpl
    extends CommandSupport
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @PostConstruct
    private void init() {
        //
        // TODO: Should ask the container to auto-wire these folks, or ask for defalut versions from the container via factory?
        //       or simply use a parent bean to configure them?
        //
        
        // Create default components if not configured
        if (getDocumenter() == null) {
            setDocumenter(new CommandDocumenterImpl());
        }
        if (getCompleter() == null) {
            setCompleter(new CommandCompleterImpl());
        }
        if (getMessages() == null) {
            setMessages(new ResourceBundleMessageSource(getAction().getClass()));
        }
    }
    
    public CommandResult execute(final Object[] args, final IO io, final Variables variables) {
        assert args != null;
        assert io != null;
        assert variables != null;

        log.trace("Executing");

        // Provide logging context for the command execution
        MDC.put("command-id", getId());

        CommandResult result;

        try {
            CommandAction action = getAction();

            // Setup the command action
            try {
                // Process command line options/arguments
                if (processArguments(io, action, args)) {
                    // return if we have been asked to display --help
                    return new CommandResult(CommandAction.Result.SUCCESS);
                }

                // TODO: Add preferences processor
            }
            catch (Exception e) {
                return new CommandResult(e);
            }

            // Setup the command context
            CommandContext context = new CommandContext()
            {
                public Object[] getArguments() {
                    return args;
                }

                public IO getIo() {
                    return io;
                }

                public Variables getVariables() {
                    return variables;
                }

                public Command getCommand() {
                    return CommandImpl.this;
                }
            };

            // Execute the action
            try {
                log.trace("Executing action: {}", action);

                Object value = action.execute(context);

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
            MDC.remove("command-id");
        }

        return result;
    }

    private boolean processArguments(final IO io, final CommandAction action, final Object[] args) throws Exception {
        assert io != null;
        assert action != null;
        assert args != null;

        if (log.isTraceEnabled()) {
            log.trace("Processing arguments: {}", Arguments.toStringArray(args));
        }

        CommandLineProcessor clp = new CommandLineProcessor();
        clp.addBean(action);

        // Attach some help context
        CommandDocumenter documenter = getDocumenter();
        clp.addBean(documenter);

        HelpSupport help = new HelpSupport();
        clp.addBean(help);

        // Process the arguments
        clp.process(Arguments.toStringArray(args));

        // Render command-line usage
        if (help.displayHelp) {
            log.trace("Render command-line usage");
            
            documenter.renderUsage(io.out);
            return true;
        }

        return false;
    }

    /**
     * Helper to inject <tt>--help<tt> support.  Package access to allow CommandDocumentorImpl access.
     */
    static class HelpSupport
    {
        //
        // TODO: Need to get this description into an i18n message source
        //
        
        @Option(name="-h", aliases={"--help"}, description="Display this help message", requireOverride=true)
        public boolean displayHelp;
    }
}