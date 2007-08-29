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

package org.apache.geronimo.gshell.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the framework to interactivly get input from a console
 * and "do something" with the line that was read.
 *
 * @version $Rev$ $Date$
 */
public class InteractiveConsole
    implements Runnable
{
    //
    // TODO: Rename to *Runner, since this is not really a Console impl
    //

    private static final Logger log = LoggerFactory.getLogger(InteractiveConsole.class);

    private final Console console;

    private final Executor executor;

    private final Prompter prompter;

    private boolean running = false;

    private boolean shutdownOnNull = true;

    public InteractiveConsole(final Console console, final Executor executor, final Prompter prompter) {
        assert console != null;
        assert executor != null;
        assert prompter != null;

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

        while ((line = console.readLine(doGetPrompt())) != null) {
            if (debug) {
                log.debug("Read line: " + line);

                // Log the line as hex
                StringBuffer idx = new StringBuffer();
                StringBuffer hex = new StringBuffer();

                byte[] bytes = line.getBytes();
                for (byte b : bytes) {
                    String h = Integer.toHexString(b);

                    hex.append("x").append(h).append(" ");
                    idx.append(" ").append((char)b).append("  ");
                }

                log.debug("HEX: " + hex);
                log.debug("     "  + idx);
            }

            Executor.Result result = doExecute(line);

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
        //       Use-case is that Shell might want to disallow and print a "use exit command",
        //       but Script interp wants this to exit and return control to Shell.
        //
    }

    protected Executor.Result doExecute(final String line) throws Exception {
        return executor.execute(line);
    }

    protected String doGetPrompt() {
        return prompter.getPrompt();
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
        /**
         * Return the prompt to be displayed.
         *
         * @return  The prompt to be displayed; must not be null
         */
        String getPrompt();
    }
}
