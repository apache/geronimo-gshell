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
import org.apache.geronimo.gshell.command.CommandCompleter;
import org.apache.geronimo.gshell.command.CommandContainer;
import org.apache.geronimo.gshell.command.CommandContainerAware;
import org.apache.geronimo.gshell.command.CommandContainerRegistry;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.CommandDocumenter;
import org.apache.geronimo.gshell.command.CommandResult;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.i18n.MessageSource;
import org.apache.geronimo.gshell.i18n.ResourceBundleMessageSource;
import org.apache.geronimo.gshell.io.IO;
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

    private MessageSource messages;

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

    public MessageSource getMessages() {
        return messages;
    }

    public void setMessages(final MessageSource messages) {
        assert messages != null;

        this.messages = messages;
    }

    @PostConstruct
    public void init() {
        // Validate properties
        assert registry != null;
        assert id != null;
        assert action != null;

        // Create default components if not configured
        if (documenter == null) {
            setDocumenter(new CommandDocumenterImpl());
        }
        if (completer == null) {
            setCompleter(new CommandCompleterImpl());
        }
        if (messages == null) {
            setMessages(new ResourceBundleMessageSource(action.getClass()));
        }
        
        // Inject ourself into CommandContainerAware instances
        Object[] children = {
            action,
            documenter,
            completer,
            messages,
        };

        for (Object child : children) {
            if (child instanceof CommandContainerAware) {
                ((CommandContainerAware)child).setCommandContainer(this);
            }
        }

        // Register ourselves
        registry.register(this);
    }
    
    public CommandResult execute(final Object[] args, final IO io, final Variables variables) {
        assert args != null;
        assert io != null;
        assert variables != null;

        log.trace("Executing; context={}");

        // Provide logging context for the command execution
        MDC.put("command-id", id);

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

                public CommandContainer getContainer() {
                    return CommandContainerImpl.this;
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