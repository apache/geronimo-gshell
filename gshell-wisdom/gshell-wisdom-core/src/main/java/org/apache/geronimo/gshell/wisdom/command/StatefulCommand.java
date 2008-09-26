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

import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandCompleter;
import org.apache.geronimo.gshell.command.CommandDocumenter;
import org.apache.geronimo.gshell.command.CommandResult;
import org.apache.geronimo.gshell.i18n.MessageSource;
import org.apache.geronimo.gshell.shell.ShellContext;

/**
 * Stateful {@link org.apache.geronimo.gshell.command.Command} component.
 *
 * @version $Rev$ $Date$
 */
public class StatefulCommand
    extends CommandSupport
{
    private final ThreadLocal<CommandAction> actionHolder = new ThreadLocal<CommandAction>();

    private String actionId;

    public String getActionId() {
        if (actionId == null) {
            throw new IllegalStateException("Missing required property: actionId");
        }

        return actionId;
    }

    public void setActionId(final String actionId) {
        assert actionId != null;

        this.actionId = actionId;
    }

    @Override
    public synchronized CommandAction getAction() {
        CommandAction action = actionHolder.get();

        if (action == null) {
            action = createAction();
            setAction(action);
        }

        return action;
    }

    @Override
    protected synchronized void setAction(final CommandAction action) {
        super.setAction(action);

        actionHolder.set(action);
    }

    private synchronized CommandAction createAction() {
        String id = getActionId();

        log.trace("Creating action for ID: {}", id);

        return getContainer().getBean(id, CommandAction.class);
    }

    private synchronized void clearAction() {
        actionHolder.remove();

        log.trace("Action cleared");
    }

    @Override
    protected CommandResult executeAction(final ShellContext context, final Object[] args) {
        try {
            return super.executeAction(context, args);
        }
        finally {
            clearAction();
        }
    }

    // Expose some of our super-classes properties for spring configuration

    @Override
    public void setDocumenter(final CommandDocumenter documenter) {
        super.setDocumenter(documenter);
    }

    @Override
    public void setCompleter(final CommandCompleter completer) {
        super.setCompleter(completer);
    }

    @Override
    public void setMessages(final MessageSource messages) {
        super.setMessages(messages);
    }
}