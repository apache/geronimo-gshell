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

package org.apache.geronimo.gshell.expression;

import junit.framework.TestCase;

/**
 * Tests for the {@link JexlExpressionEvaluator} class.
 *
 * @version $Rev$ $Date$
 */
public class JexlExpressionEvaluatorTest
    extends TestCase
{
    protected JexlExpressionEvaluator evaluator;

    protected void setUp() throws Exception {
        evaluator = new JexlExpressionEvaluator(System.getProperties());
    }

    protected void tearDown() throws Exception {
        evaluator = null;
    }

    public void testComplexDefault() throws Exception {
        String value = "${java.home}";
        String result = evaluator.parse(value);
        assertEquals(System.getProperty("java.home"), result);
    }

    public void testComplexSubst() throws Exception {
        String value = "BEFORE${java.home}AFTER";
        String result = evaluator.parse(value);
        assertEquals("BEFORE" + System.getProperty("java.home") + "AFTER", result);
    }

    public void testComplexVariable() throws Exception {
        String myvar = "this is my variable";
        evaluator.getVariables().put("my.var", myvar);

        String value = "${my.var}";
        String result = evaluator.parse(value);
        assertEquals(myvar, result);
    }

    public void testComplexFlatVariable() throws Exception {
        String myvar = "this is my variable";
        evaluator.getVariables().put("my.var", myvar);
        evaluator.getVariables().put("my", "not used");

        String value = "${my.var}";
        String result = evaluator.parse(value);
        assertEquals(myvar, result);
    }

    public void testComplexSyntaxError() throws Exception {
        String value = "${java.home";

        try {
            String result = evaluator.parse(value);
            fail("Should have thrown an exception");
        }
        catch (ExpressionEvaluator.SyntaxException expected) {
            // ignore
        }
    }

    public void testSimple() throws Exception {
        String value = "$java.home";
        String result = evaluator.parse(value);
        assertEquals(System.getProperty("java.home"), result);
    }

    public void testSimpleSubst() throws Exception {
        String value = "BEFORE$java.home AFTER";
        String result = evaluator.parse(value);
        assertEquals("BEFORE" + System.getProperty("java.home") + " AFTER", result);
    }

    public void testSimpleSubst2() throws Exception {
        String value = "BEFORE$java.home\tAFTER";
        String result = evaluator.parse(value);
        assertEquals("BEFORE" + System.getProperty("java.home") + "\tAFTER", result);
    }
}