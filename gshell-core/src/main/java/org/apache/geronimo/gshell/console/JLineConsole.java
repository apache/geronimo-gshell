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

import jline.ConsoleReader;

/**
 * A console backed up by <a href="http://jline.sf.net">JLine</a>.
 *
 * @version $Id: IO.java 399599 2006-05-04 08:13:57Z jdillon $
 */
public class JLineConsole
    implements Console
{
    private static final Log log = LogFactory.getLog(SimpleConsole.class);

    private IO io;
    private ConsoleReader reader;

    public JLineConsole(final IO io) throws IOException {
        assert io != null;

        this.io = io;
        this.reader = new ConsoleReader(io.inputStream, io.out);
    }

    public String readLine(final String prompt) throws IOException {
        assert prompt != null;

        return reader.readLine(prompt);
    }
}
