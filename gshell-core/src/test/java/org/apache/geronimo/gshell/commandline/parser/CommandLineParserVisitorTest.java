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
 * Unit tests for the {@link CommandLineParserVisitor} usage.
 *
 * @version $Id$
 */
public class CommandLineParserVisitorTest
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

    public void testVisitor1() throws Exception {
        String input = "a \"b\" 'c' d";

        ASTCommandLine cl = parse(input);

        MockCommandLineVisitor v = new MockCommandLineVisitor();

        Object result = cl.jjtAccept(v, null);
    }

    private static class MockCommandLineVisitor
        implements CommandLineParserVisitor
    {
        public Object visit(SimpleNode node, Object data) {
            System.out.println("SimpleNode: " + node + "; data: " + data);

            data = node.childrenAccept(this, data);

            return data;
        }

        public Object visit(ASTCommandLine node, Object data) {
            System.out.println("CommandLine: " + node + "; data: " + data);

            data = node.childrenAccept(this, data);

            return data;
        }

        public Object visit(ASTExpression node, Object data) {
            System.out.println("Expression: " + node + "; data: " + data);

            data = node.childrenAccept(this, data);

            return data;
        }

        public Object visit(ASTQuotedString node, Object data) {
            System.out.println("QuotedString: " + node + "; data: " + data);

            data = node.childrenAccept(this, data);

            return data;
        }

        public Object visit(ASTOpaqueString node, Object data) {
            System.out.println("OpaqueString: " + node + "; data: " + data);

            data = node.childrenAccept(this, data);

            return data;
        }

        public Object visit(ASTPlainString node, Object data) {
            System.out.println("PlainString: " + node + "; data: " + data);

            data = node.childrenAccept(this, data);

            return data;
        }
    }
}
