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
import org.apache.geronimo.gshell.commandline.parser.StringSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.ArrayList;

/**
 * Command line parser visitor which will execute command-lines as parsed.
 *
 * @version $Id$
 */
public class CommandLineExecutingVisitor
    implements CommandLineParserVisitor
{
    private static final Log log = LogFactory.getLog(CommandLineExecutingVisitor.class);

    public Object visit(final SimpleNode node, final Object data) {
        assert node != null;
        assert data != null;

        log.error("Unhandled node type: " + node.getClass().getName());

        return null;
    }

    public Object visit(final ASTCommandLine node, final Object data) {
        assert node != null;
        assert data != null;

        //
        // NOTE: Visiting children will execute seperate commands in serial
        //

        Object result = node.childrenAccept(this, data);

        return result;
    }

    public Object visit(final ASTExpression node, final Object data) {
        assert node != null;
        assert data != null;

        List args = new ArrayList(node.jjtGetNumChildren());

        node.childrenAccept(this, args);

        //
        // TODO: Execute?  Return result?
        //

        return args;
    }

    private Object appendString(final StringSupport node, final Object data) {
        assert node != null;
        assert data != null;
        assert data instanceof List;

        List args = (List)data;
        String value = node.getValue();
        args.add(value);

        return value;
    }

    public Object visit(final ASTQuotedString node, final Object data) {
        return appendString(node, data);
    }

    public Object visit(final ASTOpaqueString node, final Object data) {
        return appendString(node, data);
    }

    public Object visit(final ASTPlainString node, final Object data) {
        return appendString(node, data);
    }
}
