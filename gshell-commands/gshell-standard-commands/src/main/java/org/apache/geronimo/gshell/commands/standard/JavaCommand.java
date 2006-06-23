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
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.MessageSource;
import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.util.Arguments;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Execute a Java standard application.
 *
 * <p>By default looks for static main(String[]) to execute, but
 * you can specify a different static method that takes a String[]
 * to execute instead.
 *
 * @version $Id$
 */
public class JavaCommand
    extends CommandSupport
{
    private String methodName = "main";

    public JavaCommand() {
        super("java");
    }

    protected Options getOptions() {
        MessageSource messages = getMessageSource();

        Options options = super.getOptions();

        options.addOption(OptionBuilder.withLongOpt("method")
            .withDescription(messages.getMessage("cli.option.method"))
            .withArgName("method")
            .create('M'));

        return options;
    }

    protected String getUsage() {
        return super.getUsage() + " <classname> [arguments]";
    }

    protected int doExecute(final String[] args) throws Exception {
        assert args != null;

        MessageSource messages = getMessageSource();

        IO io = getIO();

        Options options = getOptions();

        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, args);

        boolean usage = false;
        String[] _args = line.getArgs();

        if (_args.length == 0) {
            io.err.println(messages.getMessage("cli.error.missing_classname"));
            usage = true;
        }

        if (usage || line.hasOption('h')) {
            displayHelp(options);

            return Command.SUCCESS;
        }

        if (line.hasOption('M')) {
            methodName = line.getOptionValue('M');
        }

        run(_args);

        return Command.SUCCESS;
    }

    private void run(final String[] args) throws Exception {
        assert args != null;
        assert args.length > 0;

        run(args[0], Arguments.shift(args));
    }

    private void run(final String classname, final String[] args) throws Exception {
        assert classname != null;
        assert args != null;

        Class type = Thread.currentThread().getContextClassLoader().loadClass(classname);
        log.info("Using type: " + type);

        Method method = type.getMethod(methodName, new Class[] { String[].class });
        log.info("Using method: " + method);

        log.info("Invoking w/arguments: " + Arrays.asList(args));
        Object result = method.invoke(null, new Object[] { args });

        log.info("Result: " + result);
    }
}
