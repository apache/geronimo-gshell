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
import org.apache.geronimo.gshell.command.CommandContainerRegistry;
import org.apache.geronimo.gshell.notification.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * The default {@link CommandContainer} component.
 *
 * @version $Rev$ $Date$
 */
public class CommandContainerImpl
    implements CommandContainer
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private CommandContainerRegistry registry;

    private String id;

    private CommandAction action;

    private CommandDocumenter documenter;

    private CommandCompleter completer;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        assert id != null;

        this.id = id;
    }

    public CommandAction getAction() {
        return action;
    }

    public void setAction(final CommandAction action) {
        assert action != null;
        
        this.action = action;
    }

    public CommandDocumenter getDocumenter() {
        return documenter;
    }

    public void setDocumenter(final CommandDocumenter documenter) {
        assert documenter != null;

        this.documenter = documenter;
    }

    public CommandCompleter getCompleter() {
        return completer;
    }

    public void setCompleter(final CommandCompleter completer) {
        assert completer != null;
        
        this.completer = completer;
    }

    @PostConstruct
    public void init() {
        // TODO: Validate properties
        
        assert registry != null;
        assert id != null;
        assert action != null;

        // TODO: Inject ourself into CommandContainerAware instances

        registry.register(this);
    }
    
    public CommandResult execute(final CommandContext context) {
        assert context != null;

        log.trace("Executing; context={}");

        // Provide logging context for the command execution
        MDC.put("command-id", id);

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
     * @throws ProcessingException  A failure occured while processing the command-line.
     */
    private boolean processArguments(final CommandContext context, final CommandAction action, final Object[] args) throws ProcessingException {
        assert context != null;
        assert action != null;
        assert args != null;

        //
        // TODO: Add preferences processor
        //
        
        CommandLineProcessor clp = new CommandLineProcessor();
        clp.addBean(action);

        // Attach some help context
        CommandDocumenter documenter = getDocumenter();
        assert documenter != null;

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