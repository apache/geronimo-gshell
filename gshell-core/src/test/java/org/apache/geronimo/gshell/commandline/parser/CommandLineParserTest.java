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
        CommandLineParser parser = new CommandLineParser();
        ASTCommandLine cl = parser.parse(reader);

        cl.dump("> ");

        assertNotNull(cl);

        return cl;
    }

    //
    // Comments
    //

    public void testSingleComment1() throws Exception {
        String input = "# this should be completly ignored";

        ASTCommandLine cl = parse(input);

        // Children array is lazy created, so when no children this is null
        assertNull(cl.children);
    }

    public void testSingleComment2() throws Exception {
        String input = "####";

        ASTCommandLine cl = parse(input);

        // Children array is lazy created, so when no children this is null
        assertNull(cl.children);
    }

    public void testSingleComment3() throws Exception {
        String input = "# ignored; this too";

        ASTCommandLine cl = parse(input);

        // Children array is lazy created, so when no children this is null
        assertNull(cl.children);
    }

    //
    // Strings
    //

    public void testStrings1() throws Exception {
        String input = "a b c";

        ASTCommandLine cl = parse(input);

        //
        // TODO: Verify 3 plain strings
        //
    }

    public void testStrings2() throws Exception {
        String input = "a -b --c d";

        ASTCommandLine cl = parse(input);

        //
        // TODO: Verify 4 plain strings
        //
    }

    public void testQuotedStrings1() throws Exception {
        String input = "a \"b -c\" d";

        ASTCommandLine cl = parse(input);

        //
        // TODO: Verify 2 plain strings + 1 quoted
        //
    }

    public void testOpaqueStrings1() throws Exception {
        String input = "a 'b -c' d";

        ASTCommandLine cl = parse(input);

        //
        // TODO: Verify 2 plain strings + 1 opaque
        //
    }

    //
    // Compound
    //

    public void testCompoundCommandLine1() throws Exception {
        String input = "a b c; d e f";

        ASTCommandLine cl = parse(input);

        //
        // TODO: Verify 2 expressions
        //
    }
}
