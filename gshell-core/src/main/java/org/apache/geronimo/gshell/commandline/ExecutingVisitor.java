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

import org.apache.geronimo.gshell.commandline.parser.CommandLineParserVisitor;
import org.apache.geronimo.gshell.commandline.parser.SimpleNode;
import org.apache.geronimo.gshell.commandline.parser.ASTCommandLine;
import org.apache.geronimo.gshell.commandline.parser.ASTExpression;
import org.apache.geronimo.gshell.commandline.parser.ASTQuotedString;
import org.apache.geronimo.gshell.commandline.parser.ASTOpaqueString;
import org.apache.geronimo.gshell.commandline.parser.ASTPlainString;
import org.apache.geronimo.gshell.util.Arguments;
import org.apache.geronimo.gshell.Shell;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.ArrayList;

/**
 * Visitor which will execute command-lines as parsed.
 *
 * @version $Id$
 */
public class ExecutingVisitor
    implements CommandLineParserVisitor
{
    private static final Log log = LogFactory.getLog(ExecutingVisitor.class);

    private final Shell shell;

    private final VariableExpressionParser exprParser;

    public ExecutingVisitor(final Shell shell) {
        if (shell == null) {
            throw new IllegalArgumentException("Shell is null");
        }

        this.shell = shell;
        this.exprParser = new VariableExpressionParser(shell.getVariables());
    }

    public Object visit(final SimpleNode node, final Object data) {
        assert node != null;
        // assert data != null;

        log.error("Unhandled node type: " + node.getClass().getName());

        //
        // TODO: Exception?  Means impl node does not accept
        //

        return null;
    }

    public Object visit(final ASTCommandLine node, final Object data) {
        assert node != null;
        // assert data != null;

        //
        // NOTE: Visiting children will execute seperate commands in serial
        //

        Object result = node.childrenAccept(this, data);

        return result;
    }

    public Object visit(final ASTExpression node, final Object data) {
        assert node != null;
        // assert data != null;

        // Create the argument list (cmd name + args)
        List<String> list = new ArrayList<String>(node.jjtGetNumChildren());
        node.childrenAccept(this, list);

        String[] args = (String[])list.toArray(new String[list.size()]);
        assert list.size() >= 1;

        String commandName = args[0];
        args = Arguments.shift(args);

        int result;

        try {
            result = shell.execute(commandName, args);
        }
        catch (Exception e) {
            //
            // FIXME: !!!
            //

            throw new RuntimeException(e);
        }

        return result;
    }

    private Object appendString(final String value, final Object data) {
        assert data != null;
        assert data instanceof List;

        List<String> args = (List<String>)data;
        args.add(value);

        return value;
    }

    //
    // TODO: Include parsed ${...} strings?
    //

    public Object visit(final ASTQuotedString node, final Object data) {
        String value = exprParser.parse(node.getValue());
        return appendString(value, data);
    }

    public Object visit(final ASTOpaqueString node, final Object data) {
        return appendString(node.getValue(), data);
    }

    public Object visit(final ASTPlainString node, final Object data) {
        String value = exprParser.parse(node.getValue());
        return appendString(value, data);
    }
}
