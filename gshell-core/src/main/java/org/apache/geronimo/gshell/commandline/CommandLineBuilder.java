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
import org.apache.geronimo.gshell.command.CommandExecutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    private final CommandExecutor executor;

    private final CommandLineParser parser;

    public CommandLineBuilder(final CommandExecutor executor) {
        if (executor == null) {
            throw new IllegalArgumentException("Executor is null");
        }

        this.executor = executor;
        this.parser = new CommandLineParser();
    }

    private ASTCommandLine parse(final String input) throws ParseException {
        assert input != null;

        Reader reader = new StringReader(input);
        CommandLineParser parser = new CommandLineParser();
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
            throw new IllegalArgumentException("Command line is null");
        }
        if (commandLine.trim().length() == 0) {
            throw new IllegalArgumentException("Command line is empty");
        }

        final ASTCommandLine root = parse(commandLine);
        final ExecutingVisitor visitor = new ExecutingVisitor(this.executor);

        return new CommandLine() {
            public void execute() throws Exception {
                root.jjtAccept(visitor, null);
            }
        };
    }
}
