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
import org.apache.geronimo.gshell.command.CommandCompleter;
import org.apache.geronimo.gshell.command.CommandDocumenter;
import org.apache.geronimo.gshell.i18n.MessageSource;
import org.apache.geronimo.gshell.registry.CommandRegistry;
import org.apache.geronimo.gshell.registry.NoSuchCommandException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.PrintWriter;

/**
 * Link {@link Command} component.
 *
 * Link is similar to alias in concept, but only targets a named command, then re-uses all of that commands components.
 *
 * @version $Rev$ $Date$
 */
public class LinkCommand
    extends CommandSupport
{
    @Autowired
    private CommandRegistry commandRegistry;

    private String target;

    private Command command;

    public LinkCommand(final String target) {
        assert target != null;

        this.target = target;
    }

    private Command getCommand() {
        if (command == null) {
            assert commandRegistry != null;
            assert target != null;

            try {
                command = commandRegistry.getCommand(target);
            }
            catch (NoSuchCommandException e) {
                throw new RuntimeException("Link target command not found: " + target, e);
            }
        }

        return command;
    }

    @Override
    public CommandAction getAction() {
        return getCommand().getAction();
    }

    @Override
    public CommandDocumenter getDocumenter() {
        final CommandDocumenter delegate = getCommand().getDocumenter();

        return new CommandDocumenter() {
            public String getDescription() {
                // TODO: i18n
                return "Link to: " + target;
            }

            public void renderUsage(PrintWriter out) {
                delegate.renderUsage(out);
            }

            public void renderManual(PrintWriter out) {
                delegate.renderManual(out);
            }
        };
    }

    @Override
    public CommandCompleter getCompleter() {
        return getCommand().getCompleter();
    }

    @Override
    public MessageSource getMessages() {
        return getCommand().getMessages();
    }
}