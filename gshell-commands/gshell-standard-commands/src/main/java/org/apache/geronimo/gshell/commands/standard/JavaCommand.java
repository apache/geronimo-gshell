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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.MessageSource;
import org.apache.geronimo.gshell.command.CommandException;
import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.util.Arguments;

import java.lang.reflect.Method;

/**
 * Execute a Java standard application.
 *
 * <p>By default looks for static main(String[]) to execute, but
 * you can specify a different static method that takes a String[]
 * to execute instead.
 *
 * @version $Rev$ $Date$
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
            .hasArg()
            .withArgName("method")
            .create('M'));

        return options;
    }

    protected String getUsage() {
        return super.getUsage() + " <classname> [arguments]";
    }

    protected boolean processCommandLine(final CommandLine line) throws CommandException {
        assert line != null;

        String[] args = line.getArgs();

        IO io = getIO();
        MessageSource messages = getMessageSource();

        if (args.length == 0) {
            io.err.println(messages.getMessage("cli.error.missing_classname"));

            return true;
        }
        if (line.hasOption('M')) {
            methodName = line.getOptionValue('M');
        }

        return false;
    }

    protected Object doExecute(final Object[] args) throws Exception {
        assert args != null;

        run(String.valueOf(args[0]), Arguments.toStringArray(Arguments.shift(args)));

        return Command.SUCCESS;
    }


    private void run(final String classname, final String[] args) throws Exception {
        assert classname != null;
        assert args != null;

        boolean info = log.isInfoEnabled();

        Class type = Thread.currentThread().getContextClassLoader().loadClass(classname);
        if (info) {
            log.info("Using type: " + type);
        }

        Method method = type.getMethod(methodName, String[].class);
        if (info) {
            log.info("Using method: " + method);
        }

        if (info) {
            log.info("Invoking w/arguments: " + Arguments.asString(args));
        }

        Object result = method.invoke(null, args);

        if (info) {
            log.info("Result: " + result);
        }
    }
}
