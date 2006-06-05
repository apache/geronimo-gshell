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

/**
 * Unit test for the {@link VariableExpressionParser} class.
 *
 * @version $Id$
 */
public class VariableExpressionParserTest
    extends TestCase
{
    protected VariableExpressionParser parser;

    protected void setUp() throws Exception {
        parser = new VariableExpressionParser();
    }

    protected void tearDown() throws Exception {
        parser = null;
    }

    public void testComplexDefault() throws Exception {
        String value = "${java.home}";
        String result = parser.parse(value);
        assertEquals(System.getProperty("java.home"), result);
    }

    public void testComplexSubst() throws Exception {
        String value = "BEFORE${java.home}AFTER";
        String result = parser.parse(value);
        assertEquals("BEFORE" + System.getProperty("java.home") + "AFTER", result);
    }

    public void testComplexVariable() throws Exception {
        String myvar = "this is my variable";
        parser.setVariable("my.var", myvar);

        String value = "${my.var}";
        String result = parser.parse(value);
        assertEquals(myvar, result);
    }

    public void testComplexFlatVariable() throws Exception {
        String myvar = "this is my variable";
        parser.setVariable("my.var", myvar);
        parser.setVariable("my", "not used");

        String value = "${my.var}";
        String result = parser.parse(value);
        assertEquals(myvar, result);
    }

    public void testComplexSyntaxError() throws Exception {
        String value = "${java.home";

        try {
            String result = parser.parse(value);
            fail("Should have thrown an exception");
        }
        catch (VariableExpressionParser.SyntaxException expected) {
            // ignore
        }
    }

    public void testSimple() throws Exception {
        String value = "$java.home";
        String result = parser.parse(value);
        assertEquals(System.getProperty("java.home"), result);
    }

    public void testSimpleSubst() throws Exception {
        String value = "BEFORE$java.home AFTER";
        String result = parser.parse(value);
        assertEquals("BEFORE" + System.getProperty("java.home") + " AFTER", result);
    }

    public void testSimpleSubst2() throws Exception {
        String value = "BEFORE$java.home\tAFTER";
        String result = parser.parse(value);
        assertEquals("BEFORE" + System.getProperty("java.home") + "\tAFTER", result);
    }
}