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

package org.apache.geronimo.gshell.commands.standard;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.StringUtils;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.MessageSource;
import org.apache.geronimo.gshell.console.IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Concatenate and print files and/or URLs.
 *
 * @version $Id$
 */
public class CatCommand
    extends CommandSupport
{
    private boolean displayLineNumbers = false;

    public CatCommand() {
        super("cat");
    }

    protected int doExecute(final String[] args) throws Exception {
        assert args != null;

        MessageSource messages = getMessageSource();

        //
        // TODO: Optimize, move common code to CommandSupport
        //

        IO io = getIO();

        Options options = new Options();

        options.addOption(OptionBuilder.withLongOpt("help")
            .withDescription(messages.getMessage("cli.option.help"))
            .create('h'));

        options.addOption(OptionBuilder
            .withDescription(messages.getMessage("cli.option.n"))
            .create('n'));

        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, args);

        String[] _args = line.getArgs();

        if (line.hasOption('h')) {
            io.out.print(getName());
            io.out.print(" -- ");
            io.out.println(messages.getMessage("cli.usage.description"));
            io.out.println();

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                io.out,
                80, // width (FIXME: Should pull from gshell.columns variable)
                getName() + " [options] [<file|url> ...]",
                "",
                options,
                4, // left pad
                4, // desc pad
                "",
                false); // auto usage

            io.out.println();

            return Command.SUCCESS;
        }

        if (line.hasOption('n')) {
            displayLineNumbers = true;
        }

        // No args, then read from STDIN
        if (_args.length == 0) {
            _args = new String[] { "-" };
        }

        cat(_args);

        return Command.SUCCESS;
    }

    private void cat(final String[] files) throws IOException {
        assert files != null;

        IO io = getIO();

        for (String filename : files) {
            BufferedReader reader;

            //
            // Support "-" if length is one, and read from io.in
            // This will help test command pipelines.
            //
            if (files.length == 1 && "-".equals(files[0])) {
                log.info("Printing STDIN");
                reader = new BufferedReader(io.in);
            }
            else {
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
            }

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

            reader.close();
        }
    }
}
