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

package org.apache.geronimo.gshell.server.telnet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gshell.console.ConsoleFactory;
import org.apache.geronimo.gshell.console.Console;
import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.console.JLineConsole;
import jline.ConsoleReader;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * ???
 *
 * @version $Id$
 */
public class ConsoleFactoryImpl
    implements ConsoleFactory
{
    private static final Log log = LogFactory.getLog(ConsoleFactoryImpl.class);

    public Console create(final InputStream in, final OutputStream out) throws Exception {
        if (in == null) {
            throw new IllegalArgumentException("Input is null");
        }
        if (out == null) {
            throw new IllegalArgumentException("Output is null");
        }

        TelnetTerminal term = new TelnetTerminal(in, out);

        if (log.isDebugEnabled()) {
            log.debug("Using terminal: " + term);
            log.debug("  supported: " + term.isSupported());
            log.debug("  height: " + term.getTerminalHeight());
            log.debug("  width: " + term.getTerminalWidth());
            log.debug("  echo: " + term.getEcho());
            log.debug("  ANSI: " + term.isANSISupported());
        }

        IO io = term.getIO();
        ConsoleReader reader = new ConsoleReader(io.inputStream, io.out, /* bindings */ null, term);
        Console console = new JLineConsole(io, reader);

        return console;
    }
}