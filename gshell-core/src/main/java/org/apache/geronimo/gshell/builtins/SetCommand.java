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
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.command.MessageSource;
import org.apache.geronimo.gshell.command.VariablesImpl;
import org.apache.geronimo.gshell.command.CommandException;
import org.apache.geronimo.gshell.console.IO;

import java.util.Iterator;
import java.util.Properties;

/**
 * Set a variable or property.
 *
 * @version $Id$
 */
public class SetCommand
    extends CommandSupport
{
    enum Mode
    {
        VARIABLE,
        PROPERTY
    }

    private boolean display;

    private Mode mode = Mode.VARIABLE;

    public SetCommand() {
        super("set");
    }

    protected Options getOptions() {
        MessageSource messages = getMessageSource();

        Options options = super.getOptions();

        options.addOption(OptionBuilder.withLongOpt("property")
            .withDescription(messages.getMessage("cli.option.property"))
            .create('p'));

        return options;
    }

    protected String getUsage() {
        return super.getUsage() + " (<name[=value>])*";
    }

    protected boolean processCommandLine(final CommandLine line) throws CommandException {
        assert line != null;

        String[] args = line.getArgs();

        if (args.length == 0) {
            display = true;
        }

        if (line.hasOption('p')) {
            mode = Mode.PROPERTY;
        }

        return false;
    }

    protected Object doExecute(Object[] args) throws Exception {
        assert args != null;

        IO io = getIO();

        // No args... list all properties or variables
        if (display) {
            switch (mode) {
                case PROPERTY: {
                    Properties props = System.getProperties();
                    Iterator iter = props.keySet().iterator();

                    while (iter.hasNext()) {
                        String name = (String)iter.next();
                        String value = props.getProperty(name);

                        io.out.print(name);
                        io.out.print("=");
                        io.out.print(value);
                        io.out.println();
                    }
                    break;
                }

                case VARIABLE: {
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
                    break;
                }
            }

            return Command.SUCCESS;
        }

        //
        // FIXME: This does not jive well with the parser, and stuff like foo = "b a r"
        //

        //
        // NOTE: May want to make x=b part of the CL grammar
        //

        for (Object arg : args) {
            String namevalue = String.valueOf(arg);

            switch (mode) {
                case PROPERTY:
                    setProperty(namevalue);
                    break;

                case VARIABLE:
                    setVariable(namevalue);
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
