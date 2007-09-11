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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.jexl.resolver.FlatResolver;
import org.apache.geronimo.gshell.command.Variables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A very simple expression evalutator using Jexl to do the hard work.
 *
 * @version $Rev$ $Date$
 */
public class JexlExpressionEvaluator
    implements ExpressionEvaluator
{
    private Logger log = LoggerFactory.getLogger(getClass());

    //
    // NOTE: May want to add the ${...} bits the the CommandLineParser directly.
    //       This sub-parser is probably only short-term so we can get soemthing working
    //

    private final JexlContext context;

    public JexlExpressionEvaluator(final Map vars) {
        assert vars != null;

        context = JexlHelper.createContext();
        context.setVars(vars);


        log.trace("Using variables: {}", context.getVars());
    }

    private static Map convertToMap(final Variables vars) {
        assert vars != null;

        Map<String,Object> map = new HashMap<String,Object>();
        Iterator<String> iter = vars.names();

        while (iter.hasNext()) {
            String name = iter.next();
            map.put(name, vars.get(name));
        }

        return map;
    }

    public JexlExpressionEvaluator(final Variables vars) {
        this(convertToMap(vars));
    }

    public Map getVariables() {
        return context.getVars();
    }

    private final FlatResolver resolver = new FlatResolver(true);

    protected Expression createExpression(final String expression) throws Exception {
        assert expression != null;

        Expression expr = ExpressionFactory.createExpression(expression);
        expr.addPreResolver(resolver);

        return expr;
    }

    public Object evaluate(final String expression) throws Exception {
        assert expression != null;

        log.debug("Evaluating expression: {}", expression);

        Expression expr = createExpression(expression);
        
        Object result = expr.evaluate(context);

        log.debug("Result: {}", result);

        return result;
    }

    public String parse(final String input) throws SyntaxException {
        assert input != null;

        log.trace("Parsing input: {}", input);

        StringBuffer buff = new StringBuffer();

        int current = 0;

        while (current < input.length()) {
            boolean complex = false;

            int start = input.indexOf("$", current);

            if (start == -1) {
                break;
            }
            else if (start + 1 < input.length()) {
                if (input.charAt(start + 1) == '{') {
                    complex = true;
                }
            }

            int end;
            if (complex) {
                end = input.indexOf("}", start);
                if (end == -1) {
                    throw new SyntaxException("Missing '}': " + input);
                }
            }
            else {
                end = input.indexOf(" ", start);
                if (end == -1) {
                    end = input.indexOf("\t", start);

                    if (end == -1) {
                        end = input.length();
                    }
                }
            }

            String expr = input.substring(start + (complex ? 2 : 1), end);

            // System.err.println("b: " + buff);
            String tmp = input.substring(current, start);
            // System.err.println("t: " + tmp + "<");
            buff.append(tmp);

            try {
                buff.append(evaluate(expr));
            }
            catch (Exception e) {
                throw new SyntaxException("Failed to evaluate: " + expr, e);
            }

            // System.err.println("s:" + start);
            // System.err.println("e:" + end);
            // System.err.println("c:" + current);

            current = end + (complex ? 1 : 0);
        }

        // System.err.println("c:" + current);

        if (current < input.length()) {
            // System.err.println("b: " + buff);
            String tmp = input.substring(current);
            // System.err.println("t: " + tmp);
            buff.append(tmp);
        }

        log.trace("Parsed result: {}", buff);

        return buff.toString();
    }

    public String parse(final String input, final boolean trim) throws SyntaxException {
        String output = parse(input);
        if (trim && output != null) {
            output = output.trim();
        }

        return output;
    }
}