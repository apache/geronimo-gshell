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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.CommandExecutor;
import org.apache.geronimo.gshell.command.CommandInfo;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.common.Arguments;
import org.apache.geronimo.gshell.common.StopWatch;
import org.apache.geronimo.gshell.layout.LayoutManager;
import org.apache.geronimo.gshell.layout.NotFoundException;
import org.apache.geronimo.gshell.layout.model.AliasNode;
import org.apache.geronimo.gshell.layout.model.CommandNode;
import org.apache.geronimo.gshell.layout.model.Node;
import org.apache.geronimo.gshell.registry.CommandRegistry;
import org.apache.geronimo.gshell.registry.NotRegisteredException;
import org.apache.geronimo.gshell.shell.Environment;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default command executor.
 *
 * @version $Rev$ $Date$
 */
@Component(role=CommandExecutor.class, hint="default")
public class DefaultCommandExecutor
    implements CommandExecutor
{
    private Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private LayoutManager layoutManager;

    @Requirement
    private CommandRegistry commandRegistry;

    @Requirement
    private CommandLineBuilder commandLineBuilder;

    @Requirement
    private Environment env;

    public DefaultCommandExecutor() {}
    
    public DefaultCommandExecutor(final LayoutManager layoutManager,
                                  final CommandRegistry commandRegistry,
                                  final CommandLineBuilder commandLineBuilder,
                                  final Environment env) {
        this.layoutManager = layoutManager;
        this.commandRegistry = commandRegistry;
        this.commandLineBuilder = commandLineBuilder;
        this.env = env;
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

            ios[i] = new IO(is, os, env.getIO().errorStream);
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

            //
            // FIXME: Should not throw here, as that will cause the originating stack trace to be lost
            //

            if (t instanceof Exception) {
                throw (Exception) t;
            }
            else if (t instanceof Error) {
                throw (Error) t;
            }
            else {
                throw new RuntimeException(t);
            }
        }

        return ref.get();
    }

    protected Thread createThread(Runnable run) {
        return new Thread(run);
    }

    protected Object execute(final String path, final Object[] args, final IO io) throws Exception {
        final String searchPath = (String) env.getVariables().get(LayoutManager.COMMAND_PATH);
        
        final Node node = layoutManager.findNode(path, searchPath);
        
        final String id = findCommandId(node);

        final Command command;
        try {
            command = commandRegistry.lookup(id);
        }
        catch (NotRegisteredException e) {
            throw new NotFoundException(e.getMessage());
        }

        // Setup the command context and pass it to the command instance
        CommandContext context = new CommandContext() {
            // Command instances get their own namespace with defaults from the current
            final Variables vars = new DefaultVariables(env.getVariables());

            CommandInfo info;

            public IO getIO() {
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

        Object result;
        try {
            result = command.execute(context, args);

            log.debug("Command completed with result: {}, after: {}", result, watch);
        }
        finally {
            // Make sure that the commands output has been flushed
            try {
                env.getIO().flush();
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