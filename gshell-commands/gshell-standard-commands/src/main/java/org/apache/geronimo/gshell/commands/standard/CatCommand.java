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

package org.apache.geronimo.gshell.commands.standard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.console.IO;

/**
 * Concatenate and print files and/or URLs.
 *
 * @version $Rev$ $Date$
 */
public class CatCommand
    extends CommandSupport
{
    @Option(name="-n", description="Number the output lines, starting at 1")
    private boolean displayLineNumbers;

    @Argument(description="File or URL", required=true)
    private List<String> args;

    public CatCommand() {
        super("cat");
    }

    protected String getUsage() {
        return super.getUsage() + " [<file|url> ...]";
    }

    protected Object doExecute() throws Exception {
        IO io = getIO();

        for (String filename : args) {
            BufferedReader reader;

            // FIXME:
            /*
            //
            // Support "-" if length is one, and read from io.in
            // This will help test command pipelines.
            //
            if (files.length == 1 && "-".equals(files[0])) {
                log.info("Printing STDIN");
                reader = new BufferedReader(io.in);
            }
            else {
            */
                // First try a URL
                try {
                    URL url = new URL(filename);
                    log.info("Printing URL: " + url);
                    reader = new BufferedReader(new InputStreamReader(url.openStream()));
                }
                catch (MalformedURLException ignore) {
                    // They try a file
                    File file = new File(filename);
                    log.info("Printing file: " + file);
                    reader = new BufferedReader(new FileReader(file));
                }
            /*}*/

            String line;
            int lineno = 1;

            while ((line = reader.readLine()) != null) {
                if (displayLineNumbers) {
                    // FIXME:
                    // String gutter = StringUtils.leftPad(String.valueOf(lineno++), 6);
                    // io.out.print(gutter);


                    io.out.println(lineno++);
                    io.out.print("  ");
                }
                io.out.println(line);
            }

            reader.close();
        }

        return Command.SUCCESS;
    }
}
