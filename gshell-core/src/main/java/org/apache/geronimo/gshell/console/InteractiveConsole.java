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
import org.apache.geronimo.gshell.GShell;

/**
 * ???
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

    private final GShell gshell;

    private final Console console;

    public InteractiveConsole(final Console console, final GShell gshell) {
        if (console == null) {
            throw new IllegalArgumentException("Console is null");
        }
        if (gshell == null) {
            throw new IllegalArgumentException("GShell is null");
        }

        //
        // TODO: Can probaby abstract the GShell bits to just some kind of String executor
        //

        this.console = console;
        this.gshell = gshell;
    }

    public void run() {
        log.info("Running...");

        boolean debug = log.isDebugEnabled();

        while (true) {
            try {
                //
                // TODO: Need to resolve how to allow the prompt to be changed
                //

                String prompt = "> ";
                String line;

                while ((line = console.readLine(prompt)) != null) {
                    if (debug) {
                        log.debug("Read line: " + line);
                    }

                    // Just ignore blank lines
                    if (line.trim().equals("")) {
                        continue;
                    }

                    int result = gshell.execute(line);

                    if (debug) {
                        log.debug("Command result: " + result);
                    }
                }
            }
            catch (Exception e) {
                log.error("Unhandled failure", e);
            }
        }
    }
}
