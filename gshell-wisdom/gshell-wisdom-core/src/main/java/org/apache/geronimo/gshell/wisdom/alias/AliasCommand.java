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

package org.apache.geronimo.gshell.wisdom.alias;

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.commandline.CommandLineExecutor;
import org.apache.geronimo.gshell.i18n.MessageSource;
import org.apache.geronimo.gshell.i18n.ResourceBundleMessageSource;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.shell.ShellContext;
import org.apache.geronimo.gshell.wisdom.command.CommandSupport;
import org.apache.geronimo.gshell.wisdom.command.HelpSupport;
import org.apache.geronimo.gshell.wisdom.command.MessageSourceCommandDocumenter;
import org.apache.geronimo.gshell.wisdom.command.NullCommandCompleter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.List;

/**
 * Alias {@link org.apache.geronimo.gshell.command.Command} component.
 *
 * @version $Rev$ $Date$
 */
public class AliasCommand
    extends CommandSupport
{
    @Autowired
    private CommandLineExecutor executor;

    private String name;

    private String target;

    public AliasCommand(final String name, final String target) {
        assert name != null;
        assert target != null;

        this.name = name;
        this.target = target;

        setAction(new AliasCommandAction());
        setDocumenter(new AliasCommandDocumenter());
        setCompleter(new NullCommandCompleter());
        setMessages(new AliasCommandMessageSource());
    }

    public AliasCommand(final String name, final String target, final CommandLineExecutor executor) {
        this(name, target);

        assert executor != null;

        this.executor = executor;
    }

    @Override
    protected void prepareAction(final ShellContext context, final Object[] args) {
        // HACK: Reset state for proper appendArgs muck
        setAction(new AliasCommandAction());
        super.prepareAction(context, args);
    }

    /**
     * Action to handle invocation of the alias target + additional arguments.
     */
    private class AliasCommandAction
        implements CommandAction
    {    
        @Argument
        private List<String> appendArgs = null;

        public Object execute(final CommandContext context) throws Exception {
            assert context != null;

            ShellContext shellContext = new ShellContext() {
                public IO getIo() {
                    return context.getIo();
                }

                public Variables getVariables() {
                    return context.getVariables();
                }
            };

            StringBuilder buff = new StringBuilder();
            buff.append(target);

            // If we have args to append, then do it
            if (appendArgs != null && !appendArgs.isEmpty()) {
                buff.append(" ");
                
                Iterator iter = appendArgs.iterator();
                while (iter.hasNext()) {
                    // Append args quoted as they have already been processed by the parser
                    // HACK: Using double quote instead of single quote for now as the parser's handling of single quote is broken
                    buff.append('"').append(iter.next()).append('"');
                    if (iter.hasNext()) {
                        buff.append(" ");
                    }
                }
            }

            log.debug("Executing alias target: {}", buff);

            Object result = executor.execute(shellContext, buff.toString());

            log.debug("Alias result: {}", result);

            return result;
        }
    }

    /**
     * Alias command documenter.
     */
    private class AliasCommandDocumenter
        extends MessageSourceCommandDocumenter
    {
        public String getName() {
            return name;
        }

        public String getDescription() {
            return getMessages().format(COMMAND_DESCRIPTION, target);
        }
    }

    /**
     * Alias message source.
     */
    private class AliasCommandMessageSource
        implements MessageSource
    {
        private final MessageSource messages = new ResourceBundleMessageSource(new Class[] { AliasCommand.class, HelpSupport.class });

        public String getMessage(final String code) {
            return messages.getMessage(code);
        }

        public String format(final String code, final Object... args) {
            return messages.format(code, args);
        }
    }
}
