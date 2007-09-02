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

package org.apache.geronimo.gshell;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import jline.ConsoleReader;
import jline.History;
import org.apache.geronimo.gshell.console.Console;
import org.apache.geronimo.gshell.console.IO;

/**
 * Support for running a {@link Shell} using the <a href="http://jline.sf.net">JLine</a> library.
 *
 * @version $Rev$ $Date$
 */
public class JLineConsole
    extends Console
{
    private final ConsoleReader reader;

    // final CommandsMultiCompletor completor

    //
    // TODO: Pass in the terminal instance to be used
    //
    
    public JLineConsole(final Executor executor, final Shell shell) throws IOException {
        super(executor);

        IO io = shell.getIO();
        this.reader = new ConsoleReader(io.inputStream, new PrintWriter(io.outputStream, true));

        // this.completor = new CommandsMultiCompletor()

        // reader.addCompletor(completor)
    }

    public void run() {
        /*
        for (command in shell.registry) {
            completor << command
        }

        // Force things to become clean
        completor.refresh()
        */

        // And then actually run
        super.run();
    }

    public void setHistory(final History history) {
        reader.setHistory(history);
    }

    public void setHistoryFile(final File file) throws IOException {
        assert file != null;

        File dir = file.getParentFile();

        if (!dir.exists()) {
            dir.mkdirs();

            log.debug("Created base directory for history file: {}", dir);
        }

        log.debug("Using history file: {}", file);

        reader.getHistory().setHistoryFile(file);
    }

    protected String readLine(final String prompt) throws IOException {
        return reader.readLine(prompt);
    }
}