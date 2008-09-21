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

package org.apache.geronimo.gshell.wisdom.shell;

import org.apache.geronimo.gshell.commandline.CommandLine;
import org.apache.geronimo.gshell.commandline.CommandLineBuilder;
import org.apache.geronimo.gshell.commandline.CommandLineExecutor;
import org.apache.geronimo.gshell.notification.ErrorNotification;
import org.apache.geronimo.gshell.parser.ASTCommandLine;
import org.apache.geronimo.gshell.parser.CommandLineParser;
import org.apache.geronimo.gshell.parser.ParseException;
import org.apache.geronimo.gshell.parser.visitor.ExecutingVisitor;
import org.apache.geronimo.gshell.parser.visitor.LoggingVisitor;
import org.apache.geronimo.gshell.shell.ShellContext;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Reader;
import java.io.StringReader;

/**
 * Builds {@link CommandLine} instances ready for executing.
 *
 * @version $Rev$ $Date$
 */
public class CommandLineBuilderImpl
    implements CommandLineBuilder
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private CommandLineParser parser;
    
    public CommandLineBuilderImpl() {}

    private ASTCommandLine parse(final String input) throws ParseException {
        assert input != null;

        Reader reader = new StringReader(input);
        ASTCommandLine cl;
        try {
            cl = parser.parse(reader);
        }
        finally {
            IOUtil.close(reader);
        }

        // If debug is enabled, the log the parse tree
        if (log.isDebugEnabled()) {
            LoggingVisitor logger = new LoggingVisitor(log);
            cl.jjtAccept(logger, null);
        }

        return cl;
    }

    public CommandLine create(final String line) throws ParseException {
        assert line != null;

        log.debug("Building command-line for: {}", line);

        if (line.trim().length() == 0) {
            throw new IllegalArgumentException("Command line is empty");
        }

        try {
            final ASTCommandLine root = parse(line);

            return new CommandLine() {
                public Object execute(final ShellContext context, final CommandLineExecutor executor) throws Exception {
                    assert context != null;
                    assert executor != null;

                    ExecutingVisitor visitor = new ExecutingVisitor(context, executor);

                    return root.jjtAccept(visitor, null);
                }
            };
        }
        catch (Exception e) {
            throw new ErrorNotification(e);
        }
    }
}