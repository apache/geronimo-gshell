/*
 * Copyright 2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.gshell.commandline;

import org.apache.geronimo.gshell.commandline.parser.CommandLineParser;
import org.apache.geronimo.gshell.commandline.parser.ASTCommandLine;
import org.apache.geronimo.gshell.commandline.parser.ParseException;
import org.apache.geronimo.gshell.Shell;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.lang.NullArgumentException;

import java.io.Reader;
import java.io.StringReader;

/**
 * Builds {@link CommandLine} instances ready for executing.
 *
 * @version $Id$
 */
public class CommandLineBuilder
{
    private static final Log log = LogFactory.getLog(CommandLineBuilder.class);

    private final Shell shell;

    private final CommandLineParser parser;

    public CommandLineBuilder(final Shell shell) {
        if (shell == null) {
            throw new NullArgumentException("shell");
        }

        this.shell = shell;
        this.parser = new CommandLineParser();
    }

    private ASTCommandLine parse(final String input) throws ParseException {
        assert input != null;

        Reader reader = new StringReader(input);
        ASTCommandLine cl = parser.parse(reader);

        // If debug is enabled, the log the parse tree
        if (log.isDebugEnabled()) {
            LoggingVisitor logger = new LoggingVisitor(log);
            cl.jjtAccept(logger, null);
        }

        return cl;
    }

    public CommandLine create(final String commandLine) throws ParseException {
        if (commandLine == null) {
            throw new NullArgumentException("commandLine");
        }
        if (commandLine.trim().length() == 0) {
            throw new IllegalArgumentException("Command line is empty");
        }

        final ASTCommandLine root = parse(commandLine);
        final ExecutingVisitor visitor = new ExecutingVisitor(this.shell);

        return new CommandLine() {
            public Object execute() throws Exception {

                //
                // TODO: Handle ErrorNotification
                //

                return root.jjtAccept(visitor, null);
            }
        };
    }
}
