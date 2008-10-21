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

import org.apache.commons.vfs.FileObject;
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
    private FileObject file;

    public GroupCommand(final FileObject file) {
        // file could be null
        setFile(file);
        setAction(new GroupCommandAction());
        setDocumenter(new GroupCommandDocumenter());
        setMessages(new GroupCommandMessageSource());
    }

    public GroupCommand() {
        this(null);
    }

    //
    // FIXME: Make this a plain string, hind the mata:/commands stuff
    //
    
    public FileObject getFile() {
        if (file == null) {
            throw new IllegalStateException("Missing property: file");
        }
        return file;
    }

    public void setFile(final FileObject file) {
        this.file = file;
        if (file != null) {
            // FIXME: This isn't going to be correct, need to strip off the /commands stuff.
            String location = file.getName().getPath();
            if (location.startsWith("/commands")) {
                location = location.substring("/commands".length());
            }
            setLocation(new CommandLocationImpl(location));
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

            FileObject file = getFile();

            log.debug("Changing to group: {}", file);
            
            context.getVariables().parent().set(CommandResolver.GROUP, file);

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
            return getMessages().format(COMMAND_DESCRIPTION, getFile().getName().getBaseName());
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