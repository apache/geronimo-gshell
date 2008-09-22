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

package org.apache.geronimo.gshell.wisdom.shell;

import org.apache.geronimo.gshell.chronos.StopWatch;
import org.apache.geronimo.gshell.command.Arguments;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandException;
import org.apache.geronimo.gshell.command.CommandResolver;
import org.apache.geronimo.gshell.command.CommandResult;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.commandline.CommandLine;
import org.apache.geronimo.gshell.commandline.CommandLineBuilder;
import org.apache.geronimo.gshell.commandline.CommandLineExecutionFailied;
import org.apache.geronimo.gshell.commandline.CommandLineExecutor;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.io.SystemOutputHijacker;
import org.apache.geronimo.gshell.notification.ErrorNotification;
import org.apache.geronimo.gshell.notification.Notification;
import org.apache.geronimo.gshell.shell.ShellContext;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The default {@link CommandLineExecutor} component.
 *
 * @version $Rev$ $Date$
 */
public class CommandLineExecutorImpl
    implements CommandLineExecutor
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private CommandResolver commandResolver;

    @Autowired
    private CommandLineBuilder commandLineBuilder;

    public Object execute(final ShellContext context, final String line) throws Exception {
        assert context != null;
        assert line != null;

        log.info("Executing (String): {}", line);

        try {
            CommandLine commandLine = commandLineBuilder.create(line);

            log.trace("Command-line: {}", commandLine);
            
            return commandLine.execute(context, this);
        }
        catch (ErrorNotification n) {
            // Decode the error notifiation
            Throwable cause = n.getCause();

            if (cause instanceof Exception) {
                throw (Exception)cause;
            }
            else if (cause instanceof Error) {
                throw (Error)cause;
            }
            else {
                // Um, if we get this far, which we probably never will, then just re-toss the notifcation
                throw n;
            }
        }
    }

    public Object execute(final ShellContext context, final Object... args) throws Exception {
        assert context != null;
        assert args != null;
        assert args.length > 1;

        log.info("Executing (Object...): [{}]", Arguments.asString(args));

        return doExecute(context, String.valueOf(args[0]), Arguments.shift(args));
    }

    public Object execute(final ShellContext context, final String path, final Object[] args) throws Exception {
        assert context != null;
        assert path != null;
        assert args != null;

        log.info("Executing ({}): [{}]", path, Arguments.asString(args));

        return doExecute(context, path, args);
    }

    public Object execute(final ShellContext context, final Object[][] commands) throws Exception {
        assert context != null;
        assert commands != null;

        log.info("Executing (Object[][]): {}", Arguments.asString(commands));

        // Prepare IOs
        final IO[] ios = new IO[commands.length];
        PipedOutputStream pos = null;

        IO io = context.getIo();

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

        Thread[] threads = new Thread[commands.length];
        final List<Throwable> errors = new CopyOnWriteArrayList<Throwable>();
        final AtomicReference<Object> ref = new AtomicReference<Object>();

        for (int i = 0; i < commands.length; i++) {
            final int idx = i;

            threads[i] = createThread(new Runnable() {
                public void run() {
                    try {
                        ShellContext pipedContext = new ShellContext() {
                            public IO getIo() {
                                return ios[idx];
                            }

                            public Variables getVariables() {
                                return context.getVariables();
                            }
                        };

                        Object obj = execute(pipedContext, String.valueOf(commands[idx][0]), Arguments.shift(commands[idx]));

                        if (idx == commands.length - 1) {
                            ref.set(obj);
                        }
                    }
                    catch (Throwable t) {
                        errors.add(t);
                    }
                    finally {
                        if (idx > 0) {
                            IOUtil.close(ios[idx].inputStream);
                        }
                        if (idx < commands.length - 1) {
                            IOUtil.close(ios[idx].outputStream);
                        }
                    }
                }
            });

            threads[i].start();
        }

        for (int i = 0; i < commands.length; i++) {
            threads[i].join();
        }

        if (!errors.isEmpty()) {
            Throwable t = errors.get(0);

            // Always preserve the type of notication throwables, reguardless of the trace
            if (t instanceof Notification) {
                throw (Notification)t;
            }

            // Otherwise wrap to preserve the trace
            throw new CommandLineExecutionFailied(t);
        }

        return ref.get();
    }

    protected Thread createThread(final Runnable task) {
        return new Thread(task);
    }

    protected Object doExecute(final ShellContext context, final String path, final Object[] args) throws Exception {
        assert context != null;

        log.debug("Executing");

        // Locate the command
        Variables variables = context.getVariables();
        Command command = commandResolver.resolve(variables, path);

        // Hijack the system streams in the current thread's context
        IO io = context.getIo();
        SystemOutputHijacker.register(io.outputStream, io.errorStream);

        // Setup command timings
        StopWatch watch = new StopWatch(true);
        
        CommandResult result;
        try {
            result = command.execute(context, args);

            log.debug("Command completed with result: {}, after: {}", result, watch);
        }
        finally {
            // Restore hijacked streams
            SystemOutputHijacker.deregister();

            // Make sure that the commands output has been flushed
            try {
                io.flush();
            }
            catch (Exception ignore) {}
        }

        // Decode the command result
        if (result.hasNotified()) {
            throw result.getNotification();
        }
        else if (result.hasFailed()) {
            //noinspection ThrowableResultOfMethodCallIgnored
            throw new CommandException(result.getFailure());
        }
        else {
            return result.getValue();
        }
    }
}