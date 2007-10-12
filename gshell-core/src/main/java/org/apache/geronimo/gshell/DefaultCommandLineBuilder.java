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

package org.apache.geronimo.gshell;

import java.io.Reader;
import java.io.StringReader;

import org.apache.geronimo.gshell.command.CommandExecutor;
import org.apache.geronimo.gshell.parser.ASTCommandLine;
import org.apache.geronimo.gshell.parser.CommandLineParser;
import org.apache.geronimo.gshell.parser.ParseException;
import org.apache.geronimo.gshell.shell.Environment;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds {@link CommandLine} instances ready for executing.
 *
 * @version $Rev$ $Date$
 */
@Component(role=CommandLineBuilder.class)
public class DefaultCommandLineBuilder implements CommandLineBuilder
{
    private Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private PlexusContainer container;

    private CommandLineParser parser = new CommandLineParser();

    public DefaultCommandLineBuilder() {}
    
    public DefaultCommandLineBuilder(final PlexusContainer container) {
        this.container = container;
    }

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

    public CommandLine create(final String commandLine) throws ParseException {
        assert commandLine != null;

        if (commandLine.trim().length() == 0) {
            throw new IllegalArgumentException("Command line is empty");
        }

        try {
            CommandExecutor executor = (CommandExecutor) container.lookup(CommandExecutor.class);
            Environment env = (Environment) container.lookup(Environment.class);

            final ExecutingVisitor visitor = new ExecutingVisitor(executor, env);
            final ASTCommandLine root = parse(commandLine);

            return new CommandLine() {
                public Object execute() throws Exception {
                    return root.jjtAccept(visitor, null);
                }
            };
        }
        catch (Exception e) {
            throw new ErrorNotification(e);
        }
    }
}
