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

import jline.Completor;
import jline.History;
import org.apache.geronimo.gshell.application.Application;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.commandline.CommandLineExecutor;
import org.apache.geronimo.gshell.console.Console;
import org.apache.geronimo.gshell.console.Console.ErrorHandler;
import org.apache.geronimo.gshell.console.Console.Prompter;
import org.apache.geronimo.gshell.console.JLineConsole;
import org.apache.geronimo.gshell.console.completer.AggregateCompleter;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.model.application.Branding;
import org.apache.geronimo.gshell.notification.ExitNotification;
import org.apache.geronimo.gshell.shell.Shell;
import org.apache.geronimo.gshell.shell.ShellContext;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is the primary implementation of {@link Shell}.
 *
 * @version $Rev$ $Date$
 */
public class ShellImpl
    implements Shell
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private Application application;

    @Autowired
    private CommandLineExecutor executor;

    private History history;

    private List<Completor> completers;

    private ShellContext context;

    private Branding branding;

    private Prompter prompter;

    private ErrorHandler errorHandler;

    private boolean opened;

    private synchronized void ensureOpened() {
        if (!opened) {
            throw new IllegalStateException("Shell has not been opened or has been closed");
        }
    }

    public synchronized boolean isOpened() {
        return true;
    }

    @PostConstruct
    public synchronized void init() throws Exception {
        if (opened) {
            throw new IllegalStateException("Shell is already opened");
        }

        log.debug("Initializing");

        assert application != null;

        // Dereference some bits from the applciation context
        final IO io = application.getIo();

        //
        // TODO: Each shell should really have its own variables, using the apps vars as its parents
        //       but before we do that we need to implement a general ShellContextHolder to allow
        //       detached components access in the threads context.
        //
        final Variables vars = application.getVariables();

        context = new ShellContext() {
            public IO getIo() {
                return io;
            }

            public Variables getVariables() {
                return vars;
            }
        };

        // HACK: Add ourself to variables so commands can get to us.  Maybe need to add to ^^^ and expose in CommandContent
        vars.set("SHELL", this, true);

        // HACK: Add history for the 'history' command, since its not part of the Shell interf it can't really access it easy, resolve with ^^^
        vars.set("SHELL.HISTORY", getHistory(), true);

        // HACK: Set the default prompt here for now, probably want to get this from branding
        vars.set("prompt", "@|bold %{application.userName}|@%{application.localHost.hostName}:@|bold %{branding.name}|> ");
        
        branding = application.getModel().getBranding();

        opened = true;

        //
        // TODO: Populate variables with some defaults, like the username/hostname/etc.
        //

        loadProfileScripts();
    }

    public synchronized void close() {
        log.debug("Closing");

        opened = false;
    }
    
    public ShellContext getContext() {
        ensureOpened();

        if (context == null) {
            throw new IllegalStateException("Shell context has not been initialized");
        }
        return context;
    }

    public void setCompleters(final List<Completor> completers) {
        assert completers != null;

        this.completers = completers;
    }

    public History getHistory() {
        if (history == null) {
            throw new IllegalStateException("Missing configuration property: history");
        }
        return history;
    }

    public void setHistory(final History history) {
        this.history = history;
    }

    public boolean isInteractive() {
        return true;
    }

    public Object execute(final String line) throws Exception {
        ensureOpened();

        assert executor != null;
        return executor.execute(getContext(), line);
    }

    public Object execute(final String command, final Object[] args) throws Exception {
        ensureOpened();

        assert executor != null;
        return executor.execute(getContext(), command, args);
    }

    public Object execute(final Object... args) throws Exception {
        ensureOpened();

        assert executor != null;
        return executor.execute(getContext(), args);
    }

    public Object execute(final Object[][] commands) throws Exception {
        ensureOpened();

        assert executor != null;
        return executor.execute(getContext(), commands);
    }

    public void run(final Object... args) throws Exception {
        assert args != null;

        ensureOpened();

        log.debug("Starting interactive console; args: {}", args);

        assert branding != null;
        loadUserScript(branding.getInteractiveScriptName());

        // Setup 2 final refs to allow our executor to pass stuff back to us
        final AtomicReference<ExitNotification> exitNotifHolder = new AtomicReference<ExitNotification>();
        final AtomicReference<Object> lastResultHolder = new AtomicReference<Object>();

        // Whip up a tiny console executor that will execute shell command-lines
        Console.Executor executor = new Console.Executor() {
            public Result execute(final String line) throws Exception {
                assert line != null;

                try {
                    Object result = ShellImpl.this.execute(line);

                    lastResultHolder.set(result);
                }
                catch (ExitNotification n) {
                    exitNotifHolder.set(n);

                    return Result.STOP;
                }

                return Result.CONTINUE;
            }
        };

        IO io = getContext().getIo();

        // Setup the console runner
        JLineConsole console = new JLineConsole(executor, io);
        console.setPrompter(getPrompter());
        console.setErrorHandler(getErrorHandler());
        console.setHistory(history);

        // Attach completers if there are any
        if (completers != null) {
            // Have to use aggregate here to get the completion list to update properly
            console.addCompleter(new AggregateCompleter(completers));
        }

        // Unless the user wants us to shut up, then display a nice welcome banner
        if (!io.isQuiet()) {
            String message = branding.getWelcomeMessage();
            if (message != null) {
                io.out.print(message);

                // If we can't tell, or have something bogus then use a reasonable default
                int width = io.getTerminal().getTerminalWidth();
                if (width < 1) {
                    width = 80;
                }
                io.out.println(StringUtils.repeat("-", width - 1));

                io.out.flush();
            }
        }

        // Check if there are args, and run them and then enter interactive
        if (args.length != 0) {
            execute(args);
        }

        // And then spin up the console and go for a jog
        console.run();

        // If any exit notification occured while running, then puke it up
        ExitNotification n = exitNotifHolder.get();
        if (n != null) {
            throw n;
        }
    }

    public Prompter getPrompter() {
        if (prompter == null) {
            throw new IllegalStateException("Missing configuration property: prompter");
        }
        return prompter;
    }

    public void setPrompter(final Prompter prompter) {
        this.prompter = prompter;
    }
    
    public ErrorHandler getErrorHandler() {
        if (errorHandler == null) {
            throw new IllegalStateException("Missing configuration property: errorHandler");
        }
        return errorHandler;
    }

    public void setErrorHandler(final ErrorHandler handler) {
        this.errorHandler = handler;
    }

    //
    // Script Processing
    //

    private void loadProfileScripts() throws Exception {
        assert branding != null;

        log.debug("Loading profile scripts");
        
        // Load profile scripts if they exist
        loadSharedScript(branding.getProfileScriptName());
        loadUserScript(branding.getProfileScriptName());
    }

    private void loadScript(final File file) throws Exception {
        assert file != null;

        BufferedReader reader = new BufferedReader(new FileReader(file));

        try {
            String line;

            while ((line = reader.readLine()) != null) {
                execute(line);
            }
        }
        finally {
            IOUtil.close(reader);
        }
    }

    private void loadUserScript(final String fileName) throws Exception {
        assert fileName != null;

        File file = new File(branding.getUserDirectory(), fileName);

        if (file.exists()) {
            log.debug("Loading user-script: {}", file);

            loadScript(file);
        }
        else {
            log.debug("User script is not present: {}", file);
        }
    }

    private void loadSharedScript(final String fileName) throws Exception {
        assert fileName != null;

        File file = new File(branding.getUserDirectory(), fileName);

        if (file.exists()) {
            log.debug("Loading shared-script: {}", file);

            loadScript(file);
        }
        else {
            log.debug("Shared script is not present: {}", file);
        }
    }
}