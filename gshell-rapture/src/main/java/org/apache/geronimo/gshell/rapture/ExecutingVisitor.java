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

package org.apache.geronimo.gshell.rapture;

import org.apache.geronimo.gshell.commandline.CommandLineExecutor;
import org.apache.geronimo.gshell.notification.ErrorNotification;
import org.apache.geronimo.gshell.parser.ASTCommandLine;
import org.apache.geronimo.gshell.parser.ASTExpression;
import org.apache.geronimo.gshell.parser.ASTOpaqueString;
import org.apache.geronimo.gshell.parser.ASTPlainString;
import org.apache.geronimo.gshell.parser.ASTProcess;
import org.apache.geronimo.gshell.parser.ASTQuotedString;
import org.apache.geronimo.gshell.parser.CommandLineParserVisitor;
import org.apache.geronimo.gshell.parser.SimpleNode;
import org.apache.geronimo.gshell.shell.ShellContext;
import org.apache.geronimo.gshell.util.Arguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor which will execute command-lines as parsed.
 *
 * @version $Rev$ $Date$
 */
public class ExecutingVisitor
    implements CommandLineParserVisitor
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ShellContext env;
    
    private final CommandLineExecutor executor;

    private final VariableInterpolator interp = new VariableInterpolator();

    public ExecutingVisitor(final CommandLineExecutor executor, final ShellContext env) {
        assert executor != null;
        assert env != null;

        this.executor = executor;
        this.env = env;
    }

    public Object visit(final SimpleNode node, final Object data) {
        assert node != null;

        // It is an error if we forgot to implement a node handler
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

        Object[][] commands = new Object[node.jjtGetNumChildren()][];
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            ASTProcess proc = (ASTProcess) node.jjtGetChild(i);
            List<Object> list = new ArrayList<Object>(proc.jjtGetNumChildren());
            proc.childrenAccept(this, list);
            commands[i] = list.toArray(new Object[list.size()]);
            assert list.size() >= 1;
        }
        try {
            return executor.execute(commands);
        }
        catch (Exception e) {
            String s = Arguments.asString(commands[0]);
            for (int i = 1; i < commands.length; i++) {
                s += " | " + Arguments.asString(commands[i]);
            }
            throw new ErrorNotification("Shell execution failed; commands=" + s, e);
        }
    }

    public Object visit(ASTProcess node, Object data) {
        return null;
    }

    @SuppressWarnings({"unchecked"})
    private Object appendString(final String value, final Object data) {
        assert data != null;
        assert data instanceof List;

        List<Object> args = (List<Object>)data;
        args.add(value);

        return value;
    }

    public Object visit(final ASTQuotedString node, final Object data) {
        assert node != null;

        String value = interp.interpolate(node.getValue(), env.getVariables());

        return appendString(value, data);
    }

    public Object visit(final ASTPlainString node, final Object data) {
        assert node != null;

        String value = interp.interpolate(node.getValue(), env.getVariables());
        
        return appendString(value, data);
    }

    public Object visit(final ASTOpaqueString node, final Object data) {
        assert node != null;

        return appendString(node.getValue(), data);
    }
}
