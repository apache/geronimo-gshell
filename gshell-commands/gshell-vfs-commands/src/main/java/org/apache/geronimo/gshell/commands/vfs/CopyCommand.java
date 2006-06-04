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

package org.apache.geronimo.gshell.commands.vfs;

import org.apache.geronimo.gshell.command.MessageSource;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.console.IO;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileUtil;

/**
 * ???
 *
 * @version $Id$
 */
public class CopyCommand
    extends VFSCommandSupport
{
    public CopyCommand() {
        super("copy");
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

        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, args);

        boolean usage = false;

        String[] _args = line.getArgs();

        // Need exactly 2 args
        if (_args.length != 2) {
            usage = true;
        }

        if (usage || line.hasOption('h')) {
            io.out.print(getName());
            io.out.print(" -- ");
            io.out.println(messages.getMessage("cli.usage.description"));
            io.out.println();

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                io.out,
                80, // width (FIXME: Should pull from gshell.columns variable)
                getName() + " [options] <source> <target>",
                "",
                options,
                4, // left pad
                4, // desc pad
                "",
                false); // auto usage

            // io.out.println();
            // io.out.println(messages.getMessage("cli.usage.footer"));
            io.out.println();

            return Command.SUCCESS;
        }

        FileSystemManager fsm = getFileSystemManager();
        FileObject source = fsm.resolveFile(_args[0]);
        FileObject target = fsm.resolveFile(_args[1]);

        log.info("Copying " + source + " -> " + target);

        FileUtil.copyContent(source, target);

        return Command.SUCCESS;
    }
}