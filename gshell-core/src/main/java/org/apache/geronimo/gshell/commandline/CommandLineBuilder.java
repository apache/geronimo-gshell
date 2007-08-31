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

package org.apache.geronimo.gshell.commandline;

import java.io.Reader;
import java.io.StringReader;

import org.apache.geronimo.gshell.commandline.parser.ASTCommandLine;
import org.apache.geronimo.gshell.commandline.parser.CommandLineParser;
import org.apache.geronimo.gshell.commandline.parser.ParseException;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds {@link CommandLine} instances ready for executing.
 *
 * @version $Rev$ $Date$
 */
public class CommandLineBuilder
{
    private Logger log = LoggerFactory.getLogger(getClass());

    // @Requirement
    private PlexusContainer container;

    // @Requirement
    private CommandLineParser parser;

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
        assert commandLine != null;

        if (commandLine.trim().length() == 0) {
            throw new IllegalArgumentException("Command line is empty");
        }

        //
        // HACK: Look up the vistor... just make it work for now
        //
        
        final ExecutingVisitor visitor;
        try {
            visitor = (ExecutingVisitor) container.lookup(ExecutingVisitor.class);
        }
        catch (ComponentLookupException e) {
            throw new RuntimeException(e);
        }

        final ASTCommandLine root = parse(commandLine);
        
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
