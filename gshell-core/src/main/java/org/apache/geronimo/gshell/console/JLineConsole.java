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

import jline.ConsoleReader;
import jline.History;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.shell.Shell;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Support for running a {@link Shell} using the <a href="http://jline.sf.net">JLine</a> library.
 *
 * @version $Rev$ $Date$
 */
public class JLineConsole
    extends Console
{
    private final ConsoleReader reader;

    public JLineConsole(final Executor executor, final IO io) throws IOException {
        super(executor);

        assert io != null;

        reader = new ConsoleReader(io.inputStream, new PrintWriter(io.outputStream, true), /*bindings*/null, io.getTerminal());
        reader.setUsePagination(true);

        // TODO: Install completion handler
    }

    public void run() {
        // TODO: Update/install/whatever the completion handler
        
        // And then actually run
        super.run();
    }

    public void setHistory(final History history) {
        reader.setHistory(history);
    }

    protected String readLine(final String prompt) throws IOException {
        return reader.readLine(prompt);
    }
}