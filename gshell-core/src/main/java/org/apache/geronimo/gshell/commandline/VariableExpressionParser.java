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

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.resolver.FlatResolver;
import org.apache.commons.lang.NullArgumentException;

import org.apache.geronimo.gshell.command.Variables;

/**
 * Parser to handle ${...} expressions using
 * <a href="http://jakarta.apache.org/commons/jexl/">JEXL</a>.
 *
 * <p>
 * Supports complex ${xxx} and simple $xxx expressions
 *
 * @version $Rev$ $Date$
 */
public class VariableExpressionParser
{
    //
    // NOTE: May want to add the ${...} bits the the CommandLineParser directly.
    //       This sub-parser is probably only short-term so we can get soemthing working
    //

    private static final Log log = LogFactory.getLog(VariableExpressionParser.class);

    protected JexlContext context;

    public VariableExpressionParser(final Map vars) {
        if (vars == null) {
            throw new NullArgumentException("vars");
        }

        context = JexlHelper.createContext();
        context.setVars(vars);

        if (log.isTraceEnabled()) {
            log.trace("Using variables: " + context.getVars());
        }
    }

    private static Map convertToMap(final Variables vars) {
        if (vars == null) {
            throw new NullArgumentException("vars");
        }

        Map map = new HashMap();
        Iterator<String> iter = vars.names();

        while (iter.hasNext()) {
            String name = iter.next();
            map.put(name, vars.get(name));
        }

        return map;
    }

    public VariableExpressionParser(final Variables vars) {
        this(convertToMap(vars));
    }

    public VariableExpressionParser() {
        this(System.getProperties());
    }

    public Map getVariables() {
        return context.getVars();
    }

    public Object getVariable(final Object name) {
        if (name == null) {
            throw new NullArgumentException("name");
        }

        return getVariables().get(name);
    }

    public Object setVariable(final Object name, final Object value) {
        if (name == null) {
            throw new NullArgumentException("name");
        }

        return getVariables().put(name, value);
    }

    public Object unsetVariable(final Object name) {
        if (name == null) {
            throw new NullArgumentException("name");
        }

        return getVariables().remove(name);
    }

    public void addVariables(final Map map) {
        if (map == null) {
            throw new NullArgumentException("map");
        }

        getVariables().putAll(map);
    }

    private final FlatResolver resolver = new FlatResolver(true);

    protected Expression createExpression(final String expression) throws Exception {
        assert expression != null;

        Expression expr = ExpressionFactory.createExpression(expression);
        expr.addPreResolver(resolver);

        return expr;
    }

    public Object evaluate(final String expression) throws Exception {
        if (expression == null) {
            throw new NullArgumentException("expression");
        }

        boolean trace = log.isTraceEnabled();
        if (trace) {
            log.trace("Evaluating expression: " + expression);
        }

        Expression expr = createExpression(expression);
        Object obj = expr.evaluate(context);
        if (trace) {
            log.trace("Result: " + obj);
        }

        return obj;
    }

    public String parse(final String input) throws SyntaxException {
        if (input == null) {
            throw new NullArgumentException("input");
        }

        boolean trace = log.isTraceEnabled();
        if (trace) {
            log.trace("Parsing input: " + input);
        }

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

        if (trace) {
            log.trace("Parsed result: " + buff);
        }

        return buff.toString();
    }

    public String parse(final String input, final boolean trim) throws SyntaxException {
        String output = parse(input);
        if (trim && output != null) {
            output = output.trim();
        }

        return output;
    }

    //
    // SyntaxException
    //

    /**
     * Thrown to indicate a syntax error while parsing.
     */
    public static class SyntaxException
        extends RuntimeException
    {
        ///CLOVER:OFF

        public SyntaxException(final String msg) {
            super(msg);
        }

        public SyntaxException(final String msg, final Throwable cause) {
            super(msg, cause);
        }
    }
}