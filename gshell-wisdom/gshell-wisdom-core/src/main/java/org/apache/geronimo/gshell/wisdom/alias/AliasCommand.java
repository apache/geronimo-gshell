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

import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.i18n.MessageSource;
import org.apache.geronimo.gshell.wisdom.command.CommandDocumenterSupport;
import org.apache.geronimo.gshell.wisdom.command.CommandSupport;
import org.apache.geronimo.gshell.wisdom.command.NullCommandCompleter;
import org.apache.geronimo.gshell.commandline.CommandLineExecutor;
import org.apache.geronimo.gshell.shell.ShellContext;
import org.apache.geronimo.gshell.io.IO;
import org.springframework.beans.factory.annotation.Autowired;

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
    
    private class AliasCommandAction
        implements CommandAction
    {
        public Object execute(final CommandContext context) throws Exception {
            assert context != null;

            //
            // TODO: Take the args in the context, append them to target and execute the command via a shell, for now ignore args
            //

            ShellContext shellContext = new ShellContext() {
                public IO getIo() {
                    return context.getIo();
                }

                public Variables getVariables() {
                    return context.getVariables();
                }
            };

            log.debug("Executing alias target: {}", target);

            Object result = executor.execute(shellContext, target);

            log.debug("Alias result: {}", result);

            return result;
        }
    }

    //
    // TODO: May be able to stuff all of these into messages, since they are mostly the same for everything
    //
    
    private class AliasCommandDocumenter
        extends CommandDocumenterSupport
    {
        public String getName() {
            return name;
        }

        public String getDescription() {
            return "Alias to: " + target;
        }

        protected String getManual() {
            return "TODO: general alias manual";
        }
    }

    //
    // NOTE: This is still needed for syntax rendering and such
    //

    private class AliasCommandMessageSource
        implements MessageSource
    {
        public String getMessage(final String code) {
            return null;
        }

        public String format(final String code, final Object... args) {
            return null;
        }
    }
}
