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

package org.apache.geronimo.gshell.builtins;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.command.MessageSource;
import org.apache.geronimo.gshell.command.VariablesImpl;
import org.apache.geronimo.gshell.console.IO;

import java.util.Iterator;

/**
 * Set a variable or property.
 *
 * @version $Id$
 */
public class SetCommand
    extends CommandSupport
{
    public SetCommand() {
        super("set");
    }

    enum Mode
    {
        VARIABLE,
        PROPERTY
    }

    protected int doExecute(String[] args) throws Exception {
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

        options.addOption(OptionBuilder.withLongOpt("property")
            .withDescription(messages.getMessage("cli.option.property"))
            .create('p'));

        //
        // TODO: Add support to set immutable
        //

        //
        // TODO: Add support to set in parent (parent) scope
        //

        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, args);

        if (line.hasOption('h')) {
            io.out.print(getName());
            io.out.print(" -- ");
            io.out.println(messages.getMessage("cli.usage.description"));
            io.out.println();

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                io.out,
                80, // width (FIXME: Should pull from gshell.columns variable)
                getName() + " [options] (<name[=value>])*",
                "",
                options,
                4, // left pad
                4, // desc pad
                "",
                false); // auto usage

            io.out.println();
            io.out.println(messages.getMessage("cli.usage.footer"));
            io.out.println();

            return Command.SUCCESS;
        }

        String[] _args = line.getArgs();

        // No args... list all variables
        if (_args.length == 0) {
            Variables vars = getVariables();
            Iterator<String> iter = vars.names();

            while (iter.hasNext()) {
                String name = iter.next();
                Object value = vars.get(name);

                io.out.print(name);
                io.out.print("=");
                io.out.print(value);
                io.out.println();
            }

            return Command.SUCCESS;
        }

        Mode mode = Mode.VARIABLE;

        if (line.hasOption('p')) {
            mode = Mode.PROPERTY;
        }

        //
        // FIXME: This does not jive well with the parser, and stuff like foo = "b a r"
        //

        for (String arg : _args) {
            switch (mode) {
                case PROPERTY:
                    setProperty(arg);
                    break;

                case VARIABLE:
                    setVariable(arg);
                    break;
            }
        }

        return Command.SUCCESS;
    }

    class NameValue
    {
        String name;
        String value;
    }

    private NameValue parse(final String input) {
        NameValue nv = new NameValue();

        int i = input.indexOf("=");

        if (i == -1) {
            nv.name = input;
            nv.value = "true";
        }
        else {
            nv.name = input.substring(0, i);
            nv.value = input.substring(i + 1, input.length());
        }

        nv.name = nv.name.trim();

        return nv;
    }

    private void ensureIsIdentifier(final String name) {
        if (!VariablesImpl.isIdentifier(name)) {
            throw new RuntimeException("Invalid identifer name: " + name);
        }
    }

    private void setProperty(final String namevalue) {
        NameValue nv = parse(namevalue);

        log.info("Setting system property: " + nv.name + "=" + nv.value);

        ensureIsIdentifier(nv.name);

        System.setProperty(nv.name, nv.value);
    }

    private void setVariable(final String namevalue) {
        NameValue nv = parse(namevalue);

        log.info("Setting variable: " + nv.name + "=" + nv.value);

        ensureIsIdentifier(nv.name);

        // Command vars always has a parent, set only makes sence when setting in parent's scope
        Variables vars = this.getVariables().parent();

        vars.set(nv.name, nv.value);
    }
}
