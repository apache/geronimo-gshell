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

import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.console.IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Concatenate and print files.
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
        
        //
        // TODO: Optimize, move common code to CommandSupport
        //
        
        IO io = getIO();
        
        Options options = new Options();
        
        options.addOption(OptionBuilder.withLongOpt("help")
            .withDescription("Display this help message")
            .create('h'));
        
        options.addOption(OptionBuilder
            .withDescription("Number the output lines, starting at 1")
            .create('n'));
        
        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, args);
        
        if (line.hasOption('h')) {
            io.out.println(getName() + " -- concatenate and print files");
            io.out.println();
            
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                io.out,
                80, // width (FIXME: Should pull from gshell.columns variable)
                getName() + " [options] [file ...]",
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
        
        cat(line.getArgs());
        
        return Command.SUCCESS;
    }
    
    private void cat(final String[] files) throws IOException {
        assert files != null;
        
        IO io = getIO();
        
        for (int i=0; i<files.length; i++) {
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
                File file = new File(files[i]);
                log.info("Printing file: " + file);
                reader = new BufferedReader(new FileReader(file));
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
