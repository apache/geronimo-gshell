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

package org.apache.geronimo.gshell.parser.visitor;

import org.apache.geronimo.gshell.command.Arguments;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.commandline.CommandLineExecutionFailed;
import org.apache.geronimo.gshell.commandline.CommandLineExecutor;
import org.apache.geronimo.gshell.interpolation.VariableInterpolator;
import org.apache.geronimo.gshell.io.Closer;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.notification.ErrorNotification;
import org.apache.geronimo.gshell.notification.Notification;
import org.apache.geronimo.gshell.parser.ASTCommandLine;
import org.apache.geronimo.gshell.parser.ASTExpression;
import org.apache.geronimo.gshell.parser.ASTOpaqueString;
import org.apache.geronimo.gshell.parser.ASTPlainString;
import org.apache.geronimo.gshell.parser.ASTProcess;
import org.apache.geronimo.gshell.parser.ASTQuotedString;
import org.apache.geronimo.gshell.parser.CommandLineParserVisitor;
import org.apache.geronimo.gshell.parser.SimpleNode;
import org.apache.geronimo.gshell.shell.Shell;
import org.apache.geronimo.gshell.shell.ShellContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Visitor which will execute command-lines as parsed.
 *
 * @version $Rev$ $Date$
 */
public class ExecutingVisitor
    implements CommandLineParserVisitor
{
    private final ShellContext context;

    private final CommandLineExecutor executor;

    private final VariableInterpolator interp = new VariableInterpolator();

    public ExecutingVisitor(final ShellContext context, final CommandLineExecutor executor) {
        assert context != null;
        assert executor != null;

        this.context = context;
        this.executor = executor;
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
            return executePiped(commands);
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

        String value = interp.interpolate(node.getValue(), context.getVariables());

        return appendString(value, data);
    }

    public Object visit(final ASTPlainString node, final Object data) {
        assert node != null;

        String value = interp.interpolate(node.getValue(), context.getVariables());

        return appendString(value, data);
    }

    public Object visit(final ASTOpaqueString node, final Object data) {
        assert node != null;

        return appendString(node.getValue(), data);
    }

    protected Thread createThread(final Runnable task) {
        return new Thread(task);
    }

    private Object executePiped(final Object[][] commands) throws CommandLineExecutionFailed, InterruptedException, IOException {
        assert commands != null;

        // Prepare IOs
        final IO[] ios = new IO[commands.length];
        PipedOutputStream pos = null;

        IO io = this.context.getIo();

        for (int i = 0; i < ios.length; i++) {
            InputStream is = (i == 0) ? io.inputStream : new PipedInputStream(pos);
            OutputStream os;

            if (i == ios.length - 1) {
                os = io.outputStream;
            }
            else {
                os = pos = new PipedOutputStream();
            }

            ios[i] = new IO(is, new PrintStream(os), io.errorStream);
        }

        final List<Throwable> errors = new CopyOnWriteArrayList<Throwable>();
        final AtomicReference<Object> ref = new AtomicReference<Object>();
        final CountDownLatch latch = new CountDownLatch(commands.length);

        for (int i = 0; i < commands.length; i++) {
            final int idx = i;

            Runnable r = new Runnable() {
                public void run() {
                    try {
                        ShellContext pipedContext = new ShellContext() {
                            public Shell getShell() {
                                return ExecutingVisitor.this.context.getShell();
                            }

                            public IO getIo() {
                                return ios[idx];
                            }

                            public Variables getVariables() {
                                return ExecutingVisitor.this.context.getVariables();
                            }
                        };

                        Object obj = executor.execute(pipedContext, String.valueOf(commands[idx][0]), Arguments.shift(commands[idx]));

                        if (idx == commands.length - 1) {
                            ref.set(obj);
                        }
                    }
                    catch (Throwable t) {
                        errors.add(t);
                    }
                    finally {
                        if (idx > 0) {
                            Closer.close(ios[idx].inputStream);
                        }
                        if (idx < commands.length - 1) {
                            Closer.close(ios[idx].outputStream);
                        }
                        latch.countDown();
                    }
                }
            };
            if (idx != commands.length - 1) {
                createThread(r).start();
            } else {
                r.run();
            }
        }

        latch.await();

        if (!errors.isEmpty()) {
            Throwable t = errors.get(0);

            // Always preserve the type of notication throwables, reguardless of the trace
            if (t instanceof Notification) {
                throw (Notification)t;
            }

            // Otherwise wrap to preserve the trace
            throw new CommandLineExecutionFailed(t);
        }

        return ref.get();
    }
}
