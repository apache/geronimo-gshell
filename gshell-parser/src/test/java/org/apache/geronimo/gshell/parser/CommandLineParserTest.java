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

package org.apache.geronimo.gshell.parser;

import junit.framework.TestCase;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.Tree;

/**
 * Tests for the {@link CommandLineParser} class.
 *
 * @version $Rev$ $Date$
 */
public class CommandLineParserTest
    extends TestCase
{
    public void test1() throws Exception {
        String input = "a b 'c'; d";
        CharStream stream = new ANTLRStringStream(input);

        CommandLineLexer lex = new CommandLineLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);

        /*
        CommonTreeNodeStream nodes = new CommonTreeNodeStream((Tree)r.tree);
		nodes.setTokenStream(tokens);
		SimpleCWalker walker = new SimpleCWalker(nodes);
		walker.program();
        */
        
        CommandLineParser parser = new CommandLineParser(tokens);
        CommandLineParser.compilationUnit_return r = parser.compilationUnit();
        System.out.println("tree="+((Tree)r.tree).toStringTree());
    }
}