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
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.i18n.MessageSource;
import org.apache.geronimo.gshell.i18n.ResourceBundleMessageSource;
import org.apache.geronimo.gshell.registry.CommandResolver;
import org.apache.geronimo.gshell.wisdom.registry.CommandLocationImpl;

/**
 * Group {@link org.apache.geronimo.gshell.command.Command} component.
 *
 * @version $Rev$ $Date$
 */
public class GroupCommand
    extends CommandSupport
{
    private String path;

    public GroupCommand() {
        setAction(new GroupCommandAction());
        setDocumenter(new GroupCommandDocumenter());
        setMessages(new GroupCommandMessageSource());
    }

    public String getPath() {
        if (path == null) {
            throw new IllegalStateException("Missing property: path");
        }
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
        
        if (path != null) {
            setLocation(new CommandLocationImpl(path));
        }
    }

    /**
     * Action to set the gshell group.
     */
    private class GroupCommandAction
        implements CommandAction
    {
        public Object execute(final CommandContext context) throws Exception {
            assert context != null;

            String path = getPath();

            log.debug("Changing to group: {}", path);
            
            context.getVariables().parent().set(CommandResolver.GROUP, path);

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
        public String getDescription() {
            return getMessages().format(COMMAND_DESCRIPTION, getPath());
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