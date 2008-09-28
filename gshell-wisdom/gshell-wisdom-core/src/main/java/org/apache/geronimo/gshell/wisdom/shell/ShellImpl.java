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

import jline.History;
import jline.Completor;
import org.apache.geronimo.gshell.ansi.Renderer;
import org.apache.geronimo.gshell.ansi.Code;
import org.apache.geronimo.gshell.application.Application;
import org.apache.geronimo.gshell.application.ApplicationManager;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.commandline.CommandLineExecutor;
import org.apache.geronimo.gshell.console.Console;
import org.apache.geronimo.gshell.console.Console.ErrorHandler;
import org.apache.geronimo.gshell.console.Console.Prompter;
import org.apache.geronimo.gshell.console.JLineConsole;
import org.apache.geronimo.gshell.console.completer.AggregateCompleter;
import org.apache.geronimo.gshell.event.Event;
import org.apache.geronimo.gshell.event.EventListener;
import org.apache.geronimo.gshell.event.EventManager;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.model.application.Branding;
import org.apache.geronimo.gshell.notification.ErrorNotification;
import org.apache.geronimo.gshell.notification.ExitNotification;
import org.apache.geronimo.gshell.shell.Shell;
import org.apache.geronimo.gshell.shell.ShellContext;
import org.apache.geronimo.gshell.wisdom.application.ApplicationConfiguredEvent;
import org.codehaus.plexus.util.IOUtil;
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
    private ApplicationManager applicationManager;

    @Autowired
    private EventManager eventManager;

    @Autowired
    private CommandLineExecutor executor;

    @Autowired
    private History history;

    private List<Completor> completers;

    private ShellContext context;

    private Branding branding;

    private Prompter prompter;

    private ErrorHandler errorHandler;

    public void setCompleters(final List<Completor> completers) {
        assert completers != null;

        this.completers = completers;
    }

    public ShellContext getContext() {
        if (context == null) {
            throw new IllegalStateException("Shell context has not been initialized");
        }
        return context;
    }

    public boolean isInteractive() {
        return true;
    }

    @PostConstruct
    public void init() {
        eventManager.addListener(new EventListener() {
            public void onEvent(Event event) throws Exception {
                assert event != null;

                if (event instanceof ApplicationConfiguredEvent) {
                    ApplicationConfiguredEvent targetEvent = (ApplicationConfiguredEvent)event;

                    log.debug("Binding application io/variables/branding from context");

                    // Dereference some bits from the applciation context
                    final Application application = targetEvent.getApplication();
                    context = new ShellContext() {
                        public IO getIo() {
                            return application.getIo();
                        }

                        public Variables getVariables() {
                            //
                            // TODO: Each shell should really have its own variables, using the apps vars as its parents
                            //       but before we do that we need to implement a general ShellContextHolder to allow
                            //       detached components access in the threads context.
                            //

                            return application.getVariables();
                        }
                    };
                    
                    branding = application.getModel().getBranding();

                    //
                    // TODO: Populate variables with some defaults, like the username/hostname/etc.
                    //

                    loadProfileScripts();
                }
            }
        });
    }

    public Object execute(final String line) throws Exception {
        assert executor != null;
        return executor.execute(getContext(), line);
    }

    public Object execute(final String command, final Object[] args) throws Exception {
        assert executor != null;
        return executor.execute(getContext(), command, args);
    }

    public Object execute(final Object... args) throws Exception {
        assert executor != null;
        return executor.execute(getContext(), args);
    }

    public Object execute(final Object[][] commands) throws Exception {
        assert executor != null;
        return executor.execute(getContext(), commands);
    }

    public void run(final Object... args) throws Exception {
        assert args != null;

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
                io.out.println(message);
            }

            //
            // TODO: Render a nice line here if the branding has some property configured to enable it (move that bit out of branding's job)
            //
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
            prompter = createPrompter();
        }
        return prompter;
    }

    public void setPrompter(final Prompter prompter) {
        this.prompter = prompter;
    }

    /**
     * Allow subclasses to override the default Prompter implementation used.
     *
     * @return Interactive properter.
     */
    protected Prompter createPrompter() {
        return new Prompter() {
            Renderer renderer = new Renderer();

            //
            // TODO: Need to create a PatternPrompter, which can use interpolation of a variable to render the prompt
            //       so the following variable value would set the same prompt as we are hardcoding here:
            //
            //    set gshell.prompt="@|bold ${application.username}|@${application.localHost.hostName}:@|bold ${application.branding.name}|> "
            //

            public String prompt() {
                assert applicationManager != null;
                Application app = applicationManager.getApplication();
                Branding branding = app.getModel().getBranding();
                
                StringBuilder buff = new StringBuilder();
                buff.append(Renderer.encode(app.getUserName(), Code.BOLD));
                buff.append("@");
                buff.append(app.getLocalHost().getHostName());
                buff.append(":");
                buff.append(Renderer.encode(branding.getName(), Code.BOLD));
                buff.append("> ");

                return renderer.render(buff.toString());
            }
        };
    }

    public ErrorHandler getErrorHandler() {
        if (errorHandler == null) {
            errorHandler = createErrorHandler();
        }
        return errorHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public ErrorHandler createErrorHandler() {
        return new ErrorHandler() {
            public Result handleError(final Throwable error) {
                assert error != null;

                displayError(error);

                return Result.CONTINUE;
            }
        };
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

        IO io = getContext().getIo();

        // Spit out the terse reason why we've failed
        io.err.print("@|bold,red ERROR| ");
        io.err.print(cause.getClass().getSimpleName());
        io.err.println(": @|bold,red " + cause.getMessage() + "|");

        // Determine if the stack trace flag is set
        String stackTraceProperty = System.getProperty("gshell.show.stacktrace");
        boolean stackTraceFlag = false;
        if (stackTraceProperty != null) {
        	stackTraceFlag = stackTraceProperty.trim().equals("true");
        }

        if (io.isDebug()) {
            // If we have debug enabled then skip the fancy bits below, and log the full error, don't decode shit
            log.debug(error.toString(), error);
        }
        else if (io.isVerbose() || stackTraceFlag) {
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

                //
                // FIXME: This does not properly display the full exception detail when cause contains nested exceptions
                //

                io.err.println(buff);

                buff.setLength(0);
            }
        }
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