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

package org.apache.geronimo.gshell.commandline.parser;

import junit.framework.TestCase;

import java.io.Reader;
import java.io.StringReader;

/**
 * Unit tests for the {@link CommandLineParser} class.
 *
 * @version $Id$
 */
public class CommandLineParserTest
    extends TestCase
{
    private ASTCommandLine parse(final String input) throws ParseException {
        assert input != null;

        Reader reader = new StringReader(input);
        CommandLineParser parser = new CommandLineParser(reader);
        ASTCommandLine cl = parser.CommandLine();

        cl.dump("");

        assertNotNull(cl);

        return cl;
    }

    //
    // Comments
    //

    public void testSingleComment1() throws Exception {
        String input = "# this should be completly ignored";

        ASTCommandLine cl = parse(input);
    }

    public void testSingleComment2() throws Exception {
        String input = "####";

        ASTCommandLine cl = parse(input);
    }

    public void testSingleComment3() throws Exception {
        String input = "# ignored; this too";

        ASTCommandLine cl = parse(input);
    }

    //
    // Arguments
    //

    public void testArguments1() throws Exception {
        String input = "a b c";

        ASTCommandLine cl = parse(input);

        //
        // TODO: Verify 3 plain arguments
        //
    }

    public void testArguments2() throws Exception {
        String input = "a -b --c d";

        ASTCommandLine cl = parse(input);

        //
        // TODO: Verify 4 plain arguments
        //
    }

    public void testQuotedArguments1() throws Exception {
        String input = "a \"b -c\" d";

        ASTCommandLine cl = parse(input);

        //
        // TODO: Verify 2 plain arguments + 1 quoted
        //
    }
}
