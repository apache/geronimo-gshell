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

import junit.framework.TestCase;

import java.io.Reader;
import java.io.StringReader;

import org.apache.geronimo.gshell.commandline.parser.ASTCommandLine;
import org.apache.geronimo.gshell.commandline.parser.ParseException;
import org.apache.geronimo.gshell.commandline.parser.CommandLineParser;
import org.apache.geronimo.gshell.command.CommandExecutor;
import org.apache.geronimo.gshell.command.MockCommandExecutor;

/**
 * Unit tests for the {@link CommandLineExecutingVisitor} usage.
 *
 * @version $Id$
 */
public class CommandLineExecutingVisitorTest
    extends TestCase
{
    private ASTCommandLine parse(final String input) throws ParseException {
        assert input != null;

        Reader reader = new StringReader(input);
        CommandLineParser parser = new CommandLineParser();
        ASTCommandLine cl = parser.parse(reader);

        //
        // TODO: Remove eventually, may want to make nodes use logging to dump too
        //

        cl.dump("> ");

        assertNotNull(cl);

        return cl;
    }

    public void testConstructor() throws Exception {
        try {
            new CommandLineExecutingVisitor(null);
            fail("Accepted null value");
        }
        catch (IllegalArgumentException expected) {
            // ignore
        }

        // Happy day
        new CommandLineExecutingVisitor(new MockCommandExecutor());
    }
}
