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

package org.apache.geronimo.gshell.cli;

import org.codehaus.classworlds.ClassWorld;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.gshell.GShell;
import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.console.InteractiveConsole;
import org.apache.geronimo.gshell.console.JLineConsole;

import org.apache.geronimo.gshell.util.Version;
import org.apache.geronimo.gshell.util.Banner;

/**
 * Command-line interface to bootstrap GShell.
 *
 * @version $Id$
 */
public class Main
{
    //
    // NOTE: Do not use logging from this class, as it is used to configure
    //       the logging level with System properties, which will only get
    //       picked up on the initial loading of Log4j
    //

    private ClassWorld world;
    private IO io = new IO();
    private boolean interactive = false;

    public Main(final ClassWorld world) {
        assert world != null;
        this.world = world;

        // Default is to be quiet
        setConsoleLogLevel("WARN");
    }

    private void setConsoleLogLevel(final String level) {
        System.setProperty("gshell.log.console.level", level);
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
        
        System.setProperty(name, value);
    }

    public void run(final String[] args) throws Exception {
        assert args != null;

        StopWatch watch = new StopWatch();
        watch.start();

        Options options = new Options();

        options.addOption(OptionBuilder.withLongOpt("help")
            .withDescription("Display this help message")
            .create('h'));

        options.addOption(OptionBuilder.withLongOpt("version")
            .withDescription("Display GShell version")
            .create('V'));

        options.addOption(OptionBuilder.withLongOpt("define")
            .withDescription("Define a system property")
            .hasArg()
            .withArgName("name=value")
            .create('D'));

        options.addOption(OptionBuilder.withLongOpt("interactive")
            .withDescription("Run in interactive mode")
            .create('i'));

        //
        // TODO: Add these output modifiers to a seperate group
        //

        options.addOption(OptionBuilder.withLongOpt("debug")
            .withDescription("Enable DEBUG logging output")
            .create("debug"));

        options.addOption(OptionBuilder.withLongOpt("verbose")
            .withDescription("Enable INFO logging output")
            .create("verbose"));

        options.addOption(OptionBuilder.withLongOpt("quiet")
            .withDescription("Limit logging output to ERROR")
            .create("quiet"));

        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, args, true);

        if (line.hasOption('h')) {
            io.out.println(Banner.getBanner());

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                io.out,
                80, // width
                "gshell [options] <command> [args]",
                "",
                options,
                4, // left pad
                4, // desc pad
                "",
                false); // auto usage

            io.out.println();
            io.out.flush();

            System.exit(0);
        }

        if (line.hasOption('V')) {
            io.out.println(Banner.getBanner());
            io.out.println(Version.getInstance());
            io.out.println();
            io.out.flush();

            System.exit(0);
        }

        if (line.hasOption('D')) {
            String[] values = line.getOptionValues('D');

            for (int i=0; i<values.length; i++) {
                setPropertyFrom(values[i]);
            }
        }

        if (line.hasOption("quiet")) {
            setConsoleLogLevel("ERROR");
        }
        if (line.hasOption("debug")) {
            setConsoleLogLevel("DEBUG");
        }
        else if (line.hasOption("verbose")) {
            setConsoleLogLevel("INFO");
        }

        if (line.hasOption('i')) {
            interactive = true;
        }

        // Its okay to use logging now
        Log log = LogFactory.getLog(Main.class);

        //
        // TODO: Need to pass GShell the ClassWorld, so that the application can add to it if needed
        //

        // Startup the shell
        final GShell gshell = new GShell(io);
        String[] _args = line.getArgs();

        // Force interactive if there are no args
        if (_args.length == 0) {
            interactive = true;
        }

        log.debug("Started in " + watch);

        if (interactive) {
            //
            // TODO: May want to create a simple class in core to hide these details
            //

            InteractiveConsole console = new InteractiveConsole(
                new JLineConsole(io),
                new InteractiveConsole.Executor() {
                    public Result execute(final String line) throws Exception {
                        assert line != null;

                        // Execute unless the line is just blank
                        if (!line.trim().equals("")) {
                            gshell.execute(line);
                        }

                        return Result.CONTINUE;
                    }
                },
                new InteractiveConsole.Prompter() {
                    public String getPrompt() {
                        //
                        // TODO: Need to hook this up to allow it to change
                        //
                        
                        return "> ";
                    }
                });

            // Check if there are args, and run them and then enter interactive
            if (_args.length != 0) {
                gshell.execute(_args);
            }

            console.run();
        }
        else {
            int status = gshell.execute(_args);
            System.exit(status);
        }
    }
    
    public static void main(final String[] args, final ClassWorld world) throws Exception {
        assert args != null;
        assert world != null;

        Main main = new Main(world);
        main.run(args);
    }
    
    public static void main(final String[] args) throws Exception {
        assert args != null;
        
        ClassWorld world = new ClassWorld();
        main(args, world);
    }
}
