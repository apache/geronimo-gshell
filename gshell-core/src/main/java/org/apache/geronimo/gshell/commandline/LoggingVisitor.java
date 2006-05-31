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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

/**
 * Visitor whichs logs nodes in the tree.
 *
 * @version $Id$
 */
public class LoggingVisitor
    implements CommandLineParserVisitor
{
    public static enum Level {
        INFO,
        DEBUG
    }

    private final Log log;

    private final Level level;

    private int indent = 0;

    public LoggingVisitor() {
        this(LogFactory.getLog(LoggingVisitor.class));
    }

    public LoggingVisitor(final Log log) {
        this(log, Level.DEBUG);
    }

    public LoggingVisitor(final Log log, final Level level) {
        if (log == null) {
            throw new IllegalArgumentException("Log is null");
        }
        if (level == null) {
            throw new IllegalArgumentException("Level is null");
        }

        this.log = log;
        this.level = level;
    }

    private Object log(final Class type, final SimpleNode node, Object data) {
        // Short-circuit of logging level does not match
        switch (level) {
            case INFO:
                if (!log.isInfoEnabled()) {
                    return data;
                }
                break;

            case DEBUG:
                if (!log.isDebugEnabled()) {
                    return data;
                }
                break;
        }

        StringBuffer buff = new StringBuffer();
        
        for (int i=0; i<indent; i++) {
            buff.append(" ");
        }

        buff.append(node).append(" (").append(type.getName()).append(")");
        if (data != null) {
            buff.append("; Data: ").append(data);
        }

        switch (level) {
            case INFO:
                log.info(buff);
                break;

            case DEBUG:
                log.debug(buff);
                break;
        }

        indent++;
        data = node.childrenAccept(this, data);
        indent--;

        return data;
    }

    public Object visit(final SimpleNode node, Object data) {
        return log(SimpleNode.class, node, data);
    }

    public Object visit(final ASTCommandLine node, Object data) {
        return log(ASTCommandLine.class, node, data);
    }

    public Object visit(final ASTExpression node, Object data) {
        return log(ASTExpression.class, node, data);
    }

    public Object visit(final ASTQuotedString node, Object data) {
        return log(ASTQuotedString.class, node, data);
    }

    public Object visit(final ASTOpaqueString node, Object data) {
        return log(ASTOpaqueString.class, node, data);
    }

    public Object visit(final ASTPlainString node, Object data) {
        return log(ASTPlainString.class, node, data);
    }
}
