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

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandAware;
import org.apache.geronimo.gshell.command.CommandCompleter;
import org.apache.geronimo.gshell.command.CommandDocumenter;
import org.apache.geronimo.gshell.i18n.MessageSource;

/**
 * Provides support for {@link Command} implementations.
 *
 * @version $Rev$ $Date$
 */
public abstract class CommandSupport
    implements Command
{
    private String id;

    private CommandAction action;

    private CommandDocumenter documenter;

    private CommandCompleter completer;

    private MessageSource messages;

    public String getId() {
        assert id != null;

        return id;
    }

    public void setId(final String id) {
        assert id != null;

        this.id = id;
    }

    public CommandAction getAction() {
        assert action != null;

        return action;
    }

    public void setAction(final CommandAction action) {
        assert action != null;

        handleCommandAware(action);

        this.action = action;
    }

    public CommandDocumenter getDocumenter() {
        assert documenter != null;

        return documenter;
    }

    public void setDocumenter(final CommandDocumenter documenter) {
        assert documenter != null;

        handleCommandAware(documenter);

        this.documenter = documenter;
    }

    public CommandCompleter getCompleter() {
        assert completer != null;
        
        return completer;
    }

    public void setCompleter(final CommandCompleter completer) {
        assert completer != null;

        handleCommandAware(completer);

        this.completer = completer;
    }

    public MessageSource getMessages() {
        assert messages != null;

        return messages;
    }

    public void setMessages(final MessageSource messages) {
        assert messages != null;

        handleCommandAware(messages);

        this.messages = messages;
    }

    protected void handleCommandAware(final Object target) {
        assert target != null;

        if (target instanceof CommandAware) {
            ((CommandAware)target).setCommand(this);
        }
    }
}