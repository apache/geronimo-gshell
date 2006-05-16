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

import java.io.BufferedReader;
import java.io.IOException;

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
    private IO io;
    private BufferedReader reader;

    public InteractiveConsole(final IO io, final GShell gshell) {
        assert io != null;
        assert gshell != null;

        this.io = io;
        this.gshell = gshell;

        reader = new BufferedReader(io.in);
    }

    public void run() {
        log.info("Running...");

        while (true) {
            try {
                String line;

                while ((line = readLine("> ")) != null) {
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

    private String readLine(final String prompt) throws IOException {
        assert prompt != null;

        io.out.print(prompt);
        io.out.flush();

        return reader.readLine();
    }
}
