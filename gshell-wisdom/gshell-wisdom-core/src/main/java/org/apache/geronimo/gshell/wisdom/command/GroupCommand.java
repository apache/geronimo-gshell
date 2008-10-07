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

import org.apache.commons.vfs.FileName;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.i18n.MessageSource;
import org.apache.geronimo.gshell.i18n.ResourceBundleMessageSource;

import java.util.List;

/**
 * Group {@link org.apache.geronimo.gshell.command.Command} component.
 *
 * @version $Rev$ $Date$
 */
public class GroupCommand
    extends CommandSupport
{
    private final FileName name;

    public GroupCommand(final FileName name) {
        assert name != null;

        this.name = name;

        setAction(new GroupCommandAction());
        setDocumenter(new GroupCommandDocumenter());
        setCompleter(new NullCommandCompleter());
        setMessages(new GroupCommandMessageSource());
    }

    /**
     * ???
     */
    private class GroupCommandAction
        implements CommandAction
    {
        @Argument
        private List<String> appendArgs = null;

        public Object execute(final CommandContext context) throws Exception {
            assert context != null;

            // TODO:
            log.debug("Changing to group: {}", name);

            return Result.SUCCESS;
        }
    }

    /**
     * Group command documenter.
     */
    private class GroupCommandDocumenter
        extends MessageSourceCommandDocumenter
    {
        @Override
        public String getName() {
            return name.getBaseName();
        }

        @Override
        public String getDescription() {
            return getMessages().format(COMMAND_DESCRIPTION, name.getBaseName());
        }
    }

    /**
     * Group message source.
     */
    private class GroupCommandMessageSource
        implements MessageSource
    {
        private final MessageSource messages = new ResourceBundleMessageSource(new Class[] { GroupCommand.class, HelpSupport.class });

        public String getMessage(final String code) {
            return messages.getMessage(code);
        }

        public String format(final String code, final Object... args) {
            return messages.format(code, args);
        }
    }
}