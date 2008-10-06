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
import org.apache.geronimo.gshell.command.Arguments;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandAware;
import org.apache.geronimo.gshell.command.CommandCompleter;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.CommandDocumenter;
import org.apache.geronimo.gshell.command.CommandResult;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.i18n.MessageSource;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.notification.Notification;
import org.apache.geronimo.gshell.notification.FailureNotification;
import org.apache.geronimo.gshell.notification.SuccessNotification;
import org.apache.geronimo.gshell.shell.ShellContext;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.spring.BeanContainerAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides support for {@link Command} implementations.
 *
 * @version $Rev$ $Date$
 */
public abstract class CommandSupport
    implements Command, BeanContainerAware
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private CommandAction action;

    private CommandDocumenter documenter;

    private CommandCompleter completer;

    private MessageSource messages;

    public CommandAction getAction() {
        if (action == null) {
            throw new IllegalStateException("Missing required property: action");
        }

        return action;
    }

    protected void setAction(final CommandAction action) {
        assert action != null;

        handleCommandAware(action);

        log.trace("Action: {}", action);

        this.action = action;
    }

    public CommandDocumenter getDocumenter() {
        if (documenter == null) {
            throw new IllegalStateException("Missing required property: documenter");
        }

        return documenter;
    }

    protected void setDocumenter(final CommandDocumenter documenter) {
        assert documenter != null;

        handleCommandAware(documenter);

        log.trace("Documenter: {}", documenter);

        this.documenter = documenter;
    }

    public CommandCompleter getCompleter() {
        if (completer == null) {
            throw new IllegalStateException("Missing required property: completer");
        }
        
        return completer;
    }

    protected void setCompleter(final CommandCompleter completer) {
        assert completer != null;

        handleCommandAware(completer);

        log.trace("Completer: {}", completer);

        this.completer = completer;
    }

    public MessageSource getMessages() {
        if (messages == null) {
            throw new IllegalStateException("Missing required property: messages");
        }

        return messages;
    }

    protected void setMessages(final MessageSource messages) {
        assert messages != null;

        handleCommandAware(messages);

        log.trace("Messages: {}", messages);

        this.messages = messages;
    }

    protected void handleCommandAware(final Object target) {
        assert target != null;

        if (target instanceof CommandAware) {
            ((CommandAware)target).setCommand(this);
        }
    }

    // BeanContainerAware

    private BeanContainer container;

    public void setBeanContainer(final BeanContainer container) {
        assert container != null;

        this.container = container;
    }

    protected BeanContainer getContainer() {
        if (container == null) {
            throw new IllegalStateException("Bean container not configured");
        }

        return container;
    }

    // CommandAction execution
    
    public CommandResult execute(final ShellContext context, final Object[] args) {
        assert context != null;
        assert args != null;

        log.trace("Executing");

        // Set the TCL to the command bean containers realm
        final ClassLoader prevCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getContainer().getClassRealm());
        
        CommandResult result;

        try {
            // First prepare the action
            prepareAction(context, args);

            // Then execute it
            result = executeAction(context, args);
        }
        catch (AbortExecutionNotification n) {
            result = n.result;
        }
        finally {
            Thread.currentThread().setContextClassLoader(prevCL);
        }

        return result;
    }

    protected class AbortExecutionNotification
        extends Notification
    {
        public final CommandResult result;

        public AbortExecutionNotification(final CommandResult result) {
            assert result != null;

            this.result = result;
        }
    }

    protected void prepareAction(final ShellContext context, final Object[] args) {
        assert context != null;
        assert args != null;

        log.trace("Preparing action");

        IO io = context.getIo();
        CommandAction action = getAction();

        // Setup the command action
        try {
            // Process command line options/arguments
            if (processArguments(io, action, args)) {
                // Abort if we have been asked to display --help
                throw new AbortExecutionNotification(new CommandResult.ValueResult(CommandAction.Result.SUCCESS));
            }

            // TODO: Add preferences processor
        }
        catch (Exception e) {
            // Abort if preparation caused a failure
            throw new AbortExecutionNotification(new CommandResult.FailureResult(e));
        }
    }

    protected boolean processArguments(final IO io, final CommandAction action, final Object[] args) throws Exception {
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

    protected CommandResult executeAction(final ShellContext context, final Object[] args) {
        assert context != null;
        assert args != null;

        final IO io = context.getIo();

        log.trace("Executing action");

        // Setup the command context
        CommandContext ctx = new CommandContext() {
            private final Variables variables = new Variables(context.getVariables());

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
                return CommandSupport.this;
            }
        };

        CommandResult result;

        try {
            CommandAction action = getAction();
            
            log.trace("Executing action: {}", action);

            Object value = action.execute(ctx);

            log.trace("Result: {}", value);

            result = new CommandResult.ValueResult(value);
        }
        catch (final FailureNotification n) {
            log.trace("Command notified FAILURE result: " + n, n);

            io.error(n.getMessage());

            result = new CommandResult.ValueResult(CommandAction.Result.FAILURE);
        }
        catch (final SuccessNotification n) {
            log.trace("Command notified SUCCESS result: " + n, n);

            io.verbose(n.getMessage());

            result = new CommandResult.ValueResult(CommandAction.Result.SUCCESS);
        }
        catch (final Notification n) {
            log.trace("Notified: " + n, n);

            result = new CommandResult.NotificationResult(n);
        }
        catch (final Throwable t) {
            log.trace("Caught: " + t, t);

            result = new CommandResult.FailureResult(t);
        }

        return result;
    }
}