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
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.console.IO;

/**
 * Set a variable or property.
 *
 * @version $Id: CatCommand.java 399599 2006-05-04 08:13:57Z jdillon $
 */
public class SetCommand
    extends CommandSupport
{
    public SetCommand() {
        super("set");
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

        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, args);

        if (line.hasOption('h')) {
            io.out.println(getName() + " -- set a variable or property");
            io.out.println();

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                io.out,
                80, // width (FIXME: Should pull from gshell.columns variable)
                getName() + " [options] <name=value>",
                "",
                options,
                4, // left pad
                4, // desc pad
                "",
                false); // auto usage

            io.out.println();

            return Command.SUCCESS;
        }

        String[] _args = line.getArgs();
        if (args.length != 1) {
            log.error("Expected one and only one argument.");
        }
        else {
            //
            // TODO: Support setting context variables
            //
            
            setPropertyFrom(_args[0]);
        }

        return Command.SUCCESS;
    }

    private void setPropertyFrom(final String namevalue) {
        String name, value;
        int j = namevalue.indexOf("=");

        if (j == -1) {
            name = namevalue;
            value = "true";
        }
        else {
            name = namevalue.substring(0, j);
            value = namevalue.substring(j + 1, namevalue.length());
        }
        name = name.trim();

        log.info("Setting system property: " + name + "=" + value);

        System.setProperty(name, value);
    }
}
