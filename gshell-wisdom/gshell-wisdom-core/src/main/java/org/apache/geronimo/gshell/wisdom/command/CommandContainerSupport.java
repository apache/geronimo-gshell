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
 * Provides support for {@link CommandContainer} implementations.
 *
 * @version $Rev$ $Date$
 */
public abstract class CommandContainerSupport
    implements CommandContainer
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

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

        handleCommandContainerAware(action);

        this.action = action;
    }

    public CommandDocumenter getDocumenter() {
        return documenter;
    }

    public void setDocumenter(final CommandDocumenter documenter) {
        assert documenter != null;

        handleCommandContainerAware(documenter);

        this.documenter = documenter;
    }

    public CommandCompleter getCompleter() {
        return completer;
    }

    public void setCompleter(final CommandCompleter completer) {
        assert completer != null;

        handleCommandContainerAware(completer);

        this.completer = completer;
    }

    public MessageSource getMessages() {
        return messages;
    }

    public void setMessages(final MessageSource messages) {
        assert messages != null;

        handleCommandContainerAware(messages);

        this.messages = messages;
    }

    protected void handleCommandContainerAware(final Object target) {
        assert target != null;

        if (target instanceof CommandContainerAware) {
            ((CommandContainerAware)target).setCommandContainer(this);
        }
    }
}