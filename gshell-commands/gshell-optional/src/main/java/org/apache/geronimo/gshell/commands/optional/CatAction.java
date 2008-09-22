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

package org.apache.geronimo.gshell.commands.optional;

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Concatenate and print files and/or URLs.
 *
 * @version $Rev$ $Date$
 */
public class CatAction
    implements CommandAction
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Option(name="-n")
    private boolean displayLineNumbers;

    @Argument(required=true)
    private List<String> args = null;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;

        IO io = context.getIo();

        //
        // Support "-" if length is one, and read from io.in
        // This will help test command pipelines.
        //
        if (args.size() == 1 && "-".equals(args.get(0))) {
            log.info("Printing STDIN");
            cat(new BufferedReader(io.in), io);
        }
        else {
            for (String filename : args) {
                BufferedReader reader;

                // First try a URL
                try {
                    URL url = new URL(filename);
                    log.info("Printing URL: {}", url);
                    reader = new BufferedReader(new InputStreamReader(url.openStream()));
                }
                catch (MalformedURLException ignore) {
                    // They try a file
                    File file = new File(filename);
                    log.info("Printing file: {}", file);
                    reader = new BufferedReader(new FileReader(file));
                }

                try {
                    cat(reader, io);
                }
                finally {
                    IOUtil.close(reader);
                }
            }
        }

        return Result.SUCCESS;
    }

    private void cat(final BufferedReader reader, final IO io) throws IOException {
        String line;
        int lineno = 1;

        while ((line = reader.readLine()) != null) {
            if (displayLineNumbers) {
                String gutter = StringUtils.leftPad(String.valueOf(lineno++), 6);
                io.out.print(gutter);
                io.out.print("  ");
            }
            io.out.println(line);
        }
    }
}
