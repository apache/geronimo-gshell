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

import org.apache.geronimo.gshell.application.ApplicationManager;
import org.apache.geronimo.gshell.application.DefaultVariables;
import org.apache.geronimo.gshell.chronos.StopWatch;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.CommandInfo;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.command.CommandFactory;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.commandline.CommandExecutionFailied;
import org.apache.geronimo.gshell.commandline.CommandLine;
import org.apache.geronimo.gshell.commandline.CommandLineBuilder;
import org.apache.geronimo.gshell.commandline.CommandLineExecutor;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.io.SystemOutputHijacker;
import org.apache.geronimo.gshell.layout.LayoutManager;
import org.apache.geronimo.gshell.layout.NotFoundException;
import org.apache.geronimo.gshell.model.layout.AliasNode;
import org.apache.geronimo.gshell.model.layout.CommandNode;
import org.apache.geronimo.gshell.model.layout.Node;
import org.apache.geronimo.gshell.notification.ErrorNotification;
import org.apache.geronimo.gshell.notification.Notification;
import org.apache.geronimo.gshell.shell.Environment;
import org.apache.geronimo.gshell.util.Arguments;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@Component(role= CommandLineExecutor.class)
public class DefaultCommandLineExecutor
    implements CommandLineExecutor, Initializable
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private ApplicationManager applicationManager;

    @Requirement
    private LayoutManager layoutManager;

    @Requirement
    private CommandFactory commandFactory;

    @Requirement
    private CommandLineBuilder commandLineBuilder;

    private Environment env;

    public DefaultCommandLineExecutor() {}
    
    public DefaultCommandLineExecutor(final ApplicationManager applicationManager, final LayoutManager layoutManager, final CommandFactory commandFactory, final CommandLineBuilder commandLineBuilder) {
        assert applicationManager != null;
        assert layoutManager != null;
        assert commandFactory != null;
        assert commandLineBuilder != null;

        this.applicationManager = applicationManager;
        this.layoutManager = layoutManager;
        this.commandFactory = commandFactory;
        this.commandLineBuilder = commandLineBuilder;
    }

    public void initialize() throws InitializationException {
        assert applicationManager != null;
        
        this.env = applicationManager.getContext().getEnvironment();
    }

    public Object execute(final String line) throws Exception {
        assert line != null;

        log.info("Executing (String): {}", line);

        try {
            CommandLine cl = commandLineBuilder.create(line);

            return cl.execute();
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

    public Object execute(final Object... args) throws Exception {
        assert args != null;
        assert args.length > 1;

        log.info("Executing (Object...): [{}]", Arguments.asString(args));

        return execute(String.valueOf(args[0]), Arguments.shift(args), env.getIO());
    }

    public Object execute(final String path, final Object[] args) throws Exception {
        assert path != null;
        assert args != null;

        log.info("Executing ({}): [{}]", path, Arguments.asString(args));

        return execute(path, args, env.getIO());
    }

    public Object execute(final Object[][] commands) throws Exception {
        assert commands != null;

        // Prepare IOs
        final IO[] ios = new IO[commands.length];
        PipedOutputStream pos = null;

        for (int i = 0; i < ios.length; i++) {
            InputStream is = (i == 0) ? env.getIO().inputStream : new PipedInputStream(pos);
            OutputStream os;

            if (i == ios.length - 1) {
                os = env.getIO().outputStream;
            }
            else {
                os = pos = new PipedOutputStream();
            }

            ios[i] = new IO(is, new PrintStream(os), env.getIO().errorStream);
        }

        Thread[] threads = new Thread[commands.length];
        final List<Throwable> errors = new CopyOnWriteArrayList<Throwable>();
        final AtomicReference<Object> ref = new AtomicReference<Object>();

        for (int i = 0; i < commands.length; i++) {
            final int idx = i;

            threads[i] = createThread(new Runnable() {
                public void run() {
                    try {
                        Object o = execute(String.valueOf(commands[idx][0]), Arguments.shift(commands[idx]), ios[idx]);

                        if (idx == commands.length - 1) {
                            ref.set(o);
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
            throw new CommandExecutionFailied(t);
        }

        return ref.get();
    }

    protected Thread createThread(Runnable run) {
        return new Thread(run);
    }

    protected Object execute(final String path, final Object[] args, final IO io) throws Exception {
        log.debug("Executing");

        final String searchPath = (String) env.getVariables().get(LayoutManager.COMMAND_PATH);
        log.debug("Search path: {}", searchPath);

        final Node node = layoutManager.findNode(path, searchPath);
        log.debug("Layout node: {}", node);

        final String id = findCommandId(node);
        log.debug("Command ID: {}", id);
        
        final Command command;
        try {
            command = commandFactory.create(id);
        }
        catch (Exception e) {
            throw new NotFoundException(e.getMessage());
        }

        // Setup the command context and pass it to the command instance
        CommandContext context = new CommandContext() {
            // Command instances get their own namespace with defaults from the current
            final Variables vars = new DefaultVariables(env.getVariables());

            CommandInfo info;

            public Object[] getArguments() {
                return args;
            }
            
            public IO getIo() {
                return io;
            }

            public Variables getVariables() {
                return vars;
            }

            public CommandInfo getInfo() {
                if (info == null) {
                    info = new CommandInfo()
                    {
                        public String getId() {
                            return id;
                        }

                        public String getName() {
                            if (node instanceof AliasNode) {
                                return ((AliasNode)node).getCommand();
                            }

                            return node.getName();
                        }

                        public String getAlias() {
                            if (node instanceof AliasNode) {
                                return node.getName();
                            }

                            return null;
                        }

                        public String getPath() {
                            //
                            // TODO:
                            //
                            
                            return null;
                        }
                    };
                }

                return info;
            }
        };

        // Setup command timings
        StopWatch watch = new StopWatch(true);

        // Hijack the system streams in the current thread's context
        SystemOutputHijacker.register(io.outputStream, io.errorStream);

        Object result;
        try {
            result = command.execute(context);

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

        return result;
    }

    protected String findCommandId(final Node node) throws NotFoundException {
        assert node != null;

        if (node instanceof AliasNode) {
            AliasNode aliasNode = (AliasNode) node;
            String targetPath = aliasNode.getCommand();
            Node target = layoutManager.findNode(layoutManager.getLayout(), targetPath);

            return findCommandId(target);
        }
        else if (node instanceof CommandNode) {
            CommandNode commandNode = (CommandNode) node;

            return commandNode.getId();
        }

        throw new NotFoundException("Unable to get command id for: " + node);
    }
}