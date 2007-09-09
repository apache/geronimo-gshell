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

package org.apache.geronimo.gshell;

import java.util.ArrayList;
import java.util.List;

import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.common.Arguments;
import org.apache.geronimo.gshell.parser.ASTCommandLine;
import org.apache.geronimo.gshell.parser.ASTExpression;
import org.apache.geronimo.gshell.parser.ASTOpaqueString;
import org.apache.geronimo.gshell.parser.ASTPlainString;
import org.apache.geronimo.gshell.parser.ASTQuotedString;
import org.apache.geronimo.gshell.parser.CommandLineParserVisitor;
import org.apache.geronimo.gshell.parser.SimpleNode;
import org.codehaus.plexus.evaluator.EvaluatorException;
import org.codehaus.plexus.evaluator.ExpressionEvaluator;
import org.codehaus.plexus.evaluator.ExpressionSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Visitor which will execute command-lines as parsed.
 *
 * @version $Rev$ $Date$
 */
public class ExecutingVisitor
    implements CommandLineParserVisitor
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Shell shell;

    private ExpressionEvaluator evaluator;

    public ExecutingVisitor(final Shell shell, final ExpressionEvaluator evaluator) {
        assert shell != null;
        assert evaluator != null;

        this.shell = shell;
        this.evaluator = evaluator;
    }

    public Object visit(final SimpleNode node, final Object data) {
        assert node != null;

        //
        // It is an error if we forgot to implement a node handler
        //

        throw new Error("Unhandled node type: " + node.getClass().getName());
    }

    public Object visit(final ASTCommandLine node, final Object data) {
        assert node != null;

        //
        // NOTE: Visiting children will execute seperate commands in serial
        //

        return node.childrenAccept(this, data);
    }

    public Object visit(final ASTExpression node, final Object data) {
        assert node != null;

        // Create the argument list (cmd name + args)
        List<Object> list = new ArrayList<Object>(node.jjtGetNumChildren());
        node.childrenAccept(this, list);

        Object[] args = (Object[])list.toArray(new Object[list.size()]);
        assert list.size() >= 1;

        String commandName = String.valueOf(args[0]);
        args = Arguments.shift(args);

        Object result;

        try {
            result = shell.execute(commandName, args);
        }
        catch (Exception e) {
            throw new ErrorNotification(e);
        }

        return result;
    }

    private Object appendString(final String value, final Object data) {
        assert data != null;
        assert data instanceof List;

        List args = (List)data;
        args.add(value);

        return value;
    }

    //
    // TODO: Include parsed ${...} strings?
    //

    private String evaluate(final String expr) {
        final Variables vars = shell.getVariables();

        ExpressionSource source = new ExpressionSource() {
            public String getExpressionValue(String expr) {
                Object value = vars.get(expr);
                if (value != null) {
                    return String.valueOf(value);
                }

                return null;
            }
        };

        evaluator.addExpressionSource(source);

        String value = null;
        try {
            value = evaluator.expand(expr);
        }
        catch (EvaluatorException e) {
            //
            // HACK: Just make it work...
            //
            throw new RuntimeException(e);
        }
        finally {
            evaluator.removeExpressionSource(source);
        }

        return value;
    }

    public Object visit(final ASTQuotedString node, final Object data) {
        String value = evaluate(node.getValue());

        return appendString(value, data);
    }

    public Object visit(final ASTOpaqueString node, final Object data) {
        return appendString(node.getValue(), data);
    }

    public Object visit(final ASTPlainString node, final Object data) {
        String value = evaluate(node.getValue());
        
        return appendString(value, data);
    }
}
