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

import org.apache.geronimo.gshell.GShell;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ???
 *
 * @version $Id: IO.java 399599 2006-05-04 08:13:57Z jdillon $
 */
public class InteractiveConsole
    implements Runnable
{
    private static final Log log = LogFactory.getLog(InteractiveConsole.class);

    private GShell gshell;
    private Console console;

    public InteractiveConsole(final Console console, final GShell gshell) {
        assert console != null;
        assert gshell != null;

        this.console = console;
        this.gshell = gshell;
    }

    public void run() {
        log.info("Running...");

        while (true) {
            try {
                String prompt = "> ";
                String line;

                while ((line = console.readLine(prompt)) != null) {
                    log.debug("Read line: " + line);

                    // Just ignore blank lines
                    if (line.trim().equals("")) {
                        continue;
                    }

                    int result = gshell.execute(line);

                    log.debug("Command result: " + result);
                }

            }
            catch (Exception e) {
                log.error("Unhandled failure", e);
            }
        }
    }
}
