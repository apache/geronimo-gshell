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

package org.apache.geronimo.gshell.console;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides the framework to interactivly get input from a console
 * and "do something" with the line that was read.
 *
 * @version $Id$
 */
public class InteractiveConsole
    implements Runnable
{
    //
    // TODO: Rename to *Runner, since this is not really a Console impl
    //

    private static final Log log = LogFactory.getLog(InteractiveConsole.class);

    private final Console console;

    private final Executor executor;

    private final Prompter prompter;

    private boolean running = false;

    private boolean shutdownOnNull = true;

    public InteractiveConsole(final Console console, final Executor executor, final Prompter prompter) {
        if (console == null) {
            throw new IllegalArgumentException("Console is null");
        }
        if (executor == null) {
            throw new IllegalArgumentException("Executor is null");
        }
        if (prompter == null) {
            throw new IllegalArgumentException("Prompter is null");
        }

        this.console = console;
        this.executor = executor;
        this.prompter = prompter;
    }

    /**
     * Enable or disable shutting down the interactive loop when
     * a null value is read from the given console.
     *
     * @param flag  True to shutdown when a null is received; else false
     */
    public void setShutdownOnNull(final boolean flag) {
        this.shutdownOnNull = flag;
    }

    /**
     * @see #setShutdownOnNull
     */
    public boolean isShutdownOnNull() {
        return shutdownOnNull;
    }

    public boolean isRunning() {
        return running;
    }

    //
    // abort() ?
    //

    public void run() {
        log.info("Running...");

        running = true;

        while (running) {
            try {
                doRun();
            }
            catch (Exception e) {
                log.error("Exception", e);
            }
            catch (Error e) {
                log.error("Error", e);
            }
        }

        log.info("Stopped");
    }

    private void doRun() throws Exception {
        boolean debug = log.isDebugEnabled();
        String line;

        while ((line = console.readLine(prompter.getPrompt())) != null) {
            if (debug) {
                log.debug("Read line: " + line);
            }

            Executor.Result result = executor.execute(line);

            // Allow executor to request that the loop stop
            if (result == Executor.Result.STOP) {
                log.debug("Executor requested STOP");
                running = false;
                break;
            }
        }

        // Line was null, maybe shutdown
        if (shutdownOnNull) {
            log.debug("Input was null; which will cause shutdown");
            running = false;
        }

        //
        // TODO: Probably need to expose more configurability for handing/rejecting shutdown
        //
        //       Use-case is that GShell might want to disallow and print a "use exit command",
        //       but Script interp wants this to exit and return control to GShell.
        //
    }

    //
    // Executor
    //

    /**
     * Allows custom processing, the "do something".
     */
    public static interface Executor
    {
        enum Result {
            CONTINUE,
            STOP
        }

        Result execute(String line) throws Exception;
    }

    //
    // Prompter
    //

    /**
     * Allows custom prompt handling.
     */
    public static interface Prompter
    {
        String getPrompt();
    }
}
