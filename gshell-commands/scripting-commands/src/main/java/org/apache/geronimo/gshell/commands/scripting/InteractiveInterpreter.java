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

package org.apache.geronimo.gshell.commands.scripting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gshell.console.Console;
import org.apache.bsf.BSFEngine;

/**
 * ???
 *
 * @version $Id: IO.java 399599 2006-05-04 08:13:57Z jdillon $
 */
public class InteractiveInterpreter
    implements Runnable
{
    private static final Log log = LogFactory.getLog(InteractiveInterpreter.class);

    private BSFEngine engine;

    private Console console;

    private String prompt;

    public InteractiveInterpreter(final Console console, final BSFEngine engine, final String prompt) {
        assert console != null;
        assert engine != null;
        assert prompt != null;

        this.console = console;
        this.engine = engine;
        this.prompt = prompt;
    }

    public void run() {
        log.info("Running...");

        while (true) {
            try {
                String line;

                while ((line = console.readLine(prompt)) != null) {
                    log.debug("Read line: " + line);

                    //
                    // TODO: Need to handle an "exit" of some sort... ctrl-d or something... ?
                    //
                    
                    // Just ignore blank lines
                    if (line.trim().equals("")) {
                        continue;
                    }

                    engine.exec("<unknown>", 1, 1, line);
                }

            }
            catch (Exception e) {
                log.error("Unhandled failure", e);
            }
        }
    }
}
