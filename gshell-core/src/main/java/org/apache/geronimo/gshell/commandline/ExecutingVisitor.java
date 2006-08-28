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

import org.apache.geronimo.gshell.commandline.parser.CommandLineParserVisitor;
import org.apache.geronimo.gshell.commandline.parser.SimpleNode;
import org.apache.geronimo.gshell.commandline.parser.ASTCommandLine;
import org.apache.geronimo.gshell.commandline.parser.ASTExpression;
import org.apache.geronimo.gshell.commandline.parser.ASTQuotedString;
import org.apache.geronimo.gshell.commandline.parser.ASTOpaqueString;
import org.apache.geronimo.gshell.commandline.parser.ASTPlainString;
import org.apache.geronimo.gshell.util.Arguments;
import org.apache.geronimo.gshell.Shell;
import org.apache.geronimo.gshell.ErrorNotification;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.NullArgumentException;

import java.util.List;
import java.util.ArrayList;

/**
 * Visitor which will execute command-lines as parsed.
 *
 * @version $Rev$ $Date$
 */
public class ExecutingVisitor
    implements CommandLineParserVisitor
{
    private static final Log log = LogFactory.getLog(ExecutingVisitor.class);

    private final Shell shell;

    public ExecutingVisitor(final Shell shell) {
        if (shell == null) {
            throw new NullArgumentException("shell");
        }

        this.shell = shell;
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

        List<Object> args = (List<Object>)data;
        args.add(value);

        return value;
    }

    //
    // TODO: Include parsed ${...} strings?
    //

    public Object visit(final ASTQuotedString node, final Object data) {
        VariableExpressionParser exprParser = new VariableExpressionParser(shell.getVariables());
        String value = exprParser.parse(node.getValue());
        return appendString(value, data);
    }

    public Object visit(final ASTOpaqueString node, final Object data) {
        return appendString(node.getValue(), data);
    }

    public Object visit(final ASTPlainString node, final Object data) {
        VariableExpressionParser exprParser = new VariableExpressionParser(shell.getVariables());
        String value = exprParser.parse(node.getValue());
        return appendString(value, data);
    }
}
