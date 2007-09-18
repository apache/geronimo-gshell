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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.atomic.AtomicReference;

import jline.History;
import jline.Terminal;
import org.apache.geronimo.gshell.ansi.Renderer;
import org.apache.geronimo.gshell.branding.Branding;
import org.apache.geronimo.gshell.command.CommandExecutor;
import org.apache.geronimo.gshell.command.IO;
import org.apache.geronimo.gshell.console.Console;
import org.apache.geronimo.gshell.console.JLineConsole;
import org.apache.geronimo.gshell.console.TerminalInfo;
import org.apache.geronimo.gshell.shell.Environment;
import org.apache.geronimo.gshell.shell.InteractiveShell;
import org.apache.geronimo.gshell.shell.Shell;
import org.apache.geronimo.gshell.shell.ShellInfo;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the primary implementation of {@link Shell}.
 *
 * @version $Rev$ $Date$
 */
@Component(role=InteractiveShell.class)
public class DefaultShell
    implements InteractiveShell, Initializable
{
    private Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private ShellInfo shellInfo;

    @Requirement
    private Branding branding;

    @Requirement
    private CommandExecutor executor;

    @Requirement
    private TerminalInfo termInfo;

    @Requirement
    private Terminal terminal;

    @Requirement
    private Environment env;

    @Requirement
    private IO io;

    public Environment getEnvironment() {
        return env;
    }

    public ShellInfo getShellInfo() {
        return shellInfo;
    }

    public void initialize() throws InitializationException {
        //
        // FIXME: This won't work as desired, as this shell instance is not yet registered, so if a profile
        //        tries to run something that needs the shell instance... well, loopsvile.
        //
        //        This could be a warning sign that some of this class needs to be split up into smaller bits...
        //
        
        try {
            loadProfileScripts();
        }
        catch (Exception e) {
            throw new InitializationException(e.getMessage(), e);
        }
    }

    //
    // Command Execution (all delegates)
    //
    
    public Object execute(final String line) throws Exception {
        return executor.execute(line);
    }

    public Object execute(final Object... args) throws Exception {
        return executor.execute((Object[])args);
    }

    public Object execute(final String path, final Object[] args) throws Exception {
        return executor.execute(path, args);
    }

    //
    // Interactive Shell
    //

    public void run(final Object... args) throws Exception {
        assert args != null;

        log.debug("Starting interactive console; args: {}", args);

        loadUserScript(branding.getInteractiveScriptName());

        // Setup 2 final refs to allow our executor to pass stuff back to us
        final AtomicReference<ExitNotification> exitNotifHolder = new AtomicReference<ExitNotification>();
        final AtomicReference<Object> lastResultHolder = new AtomicReference<Object>();

        // Whip up a tiny console executor that will execute shell command-lines
        Console.Executor executor = new Console.Executor() {
            public Result execute(final String line) throws Exception {
                assert line != null;
                
                try {
                    Object result = DefaultShell.this.execute(line);
                    
                    lastResultHolder.set(result);
                }
                catch (ExitNotification n) {
                    exitNotifHolder.set(n);
                    
                    return Result.STOP;
                }

                return Result.CONTINUE;
            }
        };

        // Ya, bust out the sexy JLine console baby!
        JLineConsole console = new JLineConsole(executor, io, terminal);

        // Setup the prompt
        console.setPrompter(new Console.Prompter() {
            Renderer renderer = new Renderer();

            public String prompt() {
                String userName = shellInfo.getUserName();
                String hostName = shellInfo.getLocalHost().getHostName();

                //
                // HACK: There is no path... yet ;-)
                //
                String path = "/";

                return renderer.render("@|bold " + userName + "|@" + hostName + ":@|bold " + path + "|> ");
            }
        });

        // Delegate errors for display and then continue
        console.setErrorHandler(new Console.ErrorHandler() {
            public Result handleError(final Throwable error) {
                assert error != null;

                displayError(error);
                
                return Result.CONTINUE;
            }
        });

        // Hook up a nice history file (we gotta hold on to the history object at some point so the 'history' command can get to it) 
        History history = new History();
        console.setHistory(history);
        console.setHistoryFile(new File(branding.getUserDirectory(), branding.getHistoryFileName()));

        // Unless the user wants us to shut up, then display a nice welcome banner
        if (!io.isQuiet()) {
            io.out.println(branding.getWelcomeBanner());
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

    //
    // Error Display
    //

    private void displayError(final Throwable error) {
        assert error != null;

        // Decode any error notifications
        Throwable cause = error;
        if (error instanceof ErrorNotification) {
            cause = error.getCause();
        }

        // Spit out the terse reason why we've failed
        io.err.print("@|bold,red ERROR| ");
        io.err.print(cause.getClass().getSimpleName());
        io.err.println(": @|bold,red " + cause.getMessage() + "|");

        if (io.isDebug()) {
            // If we have debug enabled then skip the fancy bits below, and log the full error, don't decode shit
            log.debug(error.toString(), error);
        }
        else if (io.isVerbose()) {
            // Render a fancy ansi colored stack trace
            StackTraceElement[] trace = cause.getStackTrace();
            StringBuffer buff = new StringBuffer();

            for (StackTraceElement e : trace) {
                buff.append("        @|bold at| ").
                        append(e.getClassName()).
                        append(".").
                        append(e.getMethodName()).
                        append(" (@|bold ");

                buff.append(e.isNativeMethod() ? "Native Method" :
                            (e.getFileName() != null && e.getLineNumber() != -1 ? e.getFileName() + ":" + e.getLineNumber() :
                                (e.getFileName() != null ? e.getFileName() : "Unknown Source")));

                buff.append("|)");

                io.err.println(buff);

                buff.setLength(0);
            }
        }
    }

    //
    // Script Processing
    //

    private void loadProfileScripts() throws Exception {
        //
        // TODO: Load gsh.properties if it exists?
        //

        // Load profile scripts if they exist
        loadSharedScript(branding.getProfileScriptName());
        loadUserScript(branding.getProfileScriptName());
    }

    private void loadScript(final File file) throws Exception {
        assert file != null;

        //
        // FIXME: Don't use 'source 'for right now, the shell spins out of control from plexus component loading :-(
        //
        // execute("source", file.toURI().toURL());

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

        File file = new File(branding.getSharedDirectory(), fileName);

        if (file.exists()) {
            log.debug("Loading shared-script: {}", file);

            loadScript(file);
        }
        else {
            log.debug("Shared script is not present: {}", file);
        }
    }
}