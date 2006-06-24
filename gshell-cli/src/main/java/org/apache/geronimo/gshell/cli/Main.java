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

import org.apache.geronimo.gshell.Shell;
import org.apache.geronimo.gshell.InteractiveShell;
import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.console.Console;
import org.apache.geronimo.gshell.console.JLineConsole;

import org.apache.geronimo.gshell.util.Version;
import org.apache.geronimo.gshell.util.Banner;

import jline.Terminal;

/**
 * Command-line interface to bootstrap Shell.
 *
 * @version $Id$
 */
public class Main
{
    ///CLOVER:OFF
    
    //
    // NOTE: Do not use logging from this class, as it is used to configure
    //       the logging level with System properties, which will only get
    //       picked up on the initial loading of Log4j
    //

    private final ClassWorld world;

    private final IO io = new IO();

    private final StopWatch watch = new StopWatch();

    private boolean interactive;

    public Main(final ClassWorld world) {
        assert world != null;
        this.world = world;

        watch.start();

        // Default is to be quiet
        setConsoleLogLevel("WARN");
    }

    private void setConsoleLogLevel(final String level) {
        System.setProperty("gshell.log.console.level", level);
    }

    private void setPropertyFrom(final String namevalue) {
        String name, value;
        int i = namevalue.indexOf("=");

        if (i == -1) {
            name = namevalue;
            value = Boolean.TRUE.toString();
        }
        else {
            name = namevalue.substring(0, i);
            value = namevalue.substring(i + 1, namevalue.length());
        }
        name = name.trim();

        System.setProperty(name, value);
    }

    public void run(final String[] args) throws Exception {
        assert args != null;

        Options options = new Options();

        options.addOption(OptionBuilder.withLongOpt("help")
            .withDescription("Display this help message")
            .create('h'));

        options.addOption(OptionBuilder.withLongOpt("version")
            .withDescription("Display Shell version")
            .create('V'));

        options.addOption(OptionBuilder.withLongOpt("define")
            .withDescription("Define a system property")
            .hasArg()
            .withArgName("name=value")
            .create('D'));

        options.addOption(OptionBuilder.withLongOpt("interactive")
            .withDescription("Run in interactive mode")
            .create('i'));

        options.addOption(OptionBuilder.withLongOpt("non-interactive")
            .withDescription("Run in non-interactive mode")
            .create('n'));

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

        // Force interactive if there are no args
        if (line.getArgs().length == 0) {
            interactive = true;
        }

        if (line.hasOption('h')) {
            io.out.println(Banner.getBanner());

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                io.out,
                80, // width
                System.getProperty("program.name", "gshell") + " [options] <command> [args]",
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

            for (String value : values) {
                setPropertyFrom(value);
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
        else if (line.hasOption('n')) {
            interactive = false;
        }

        execute(line.getArgs());
    }

    private void execute(final String[] args) throws Exception {
        // Its okay to use logging now
        Log log = LogFactory.getLog(Main.class);
        boolean debug = log.isDebugEnabled();

        //
        // TODO: Need to pass Shell the ClassWorld, so that the application can add to it if needed
        //

        // Startup the shell
        final Shell gshell = new Shell(io);

        //
        // TEMP: Log some info about the terminal
        //

        Terminal term = Terminal.getTerminal();

        if (debug) {
            log.debug("Using terminal: " + term);
            log.debug("  supported: " + term.isSupported());
            log.debug("  height: " + term.getTerminalHeight());
            log.debug("  width: " + term.getTerminalWidth());
            log.debug("  echo: " + term.getEcho());
            log.debug("  ANSI: " + term.isANSISupported());
        }

        if (debug) {
            log.debug("Started in " + watch);
        }

        int status = 0;

        //
        // TODO: Pass interactive flags (maybe as property) so gshell knows what mode it is
        //

        if (interactive) {
            log.debug("Starting interactive console");

            //
            // HACK: This is JLine specific... refactor
            //

            //
            // TODO: Explicitly pass in the terminal
            //

            Console console = new JLineConsole(io);
            InteractiveShell interp = new InteractiveShell(console, gshell);

            // Check if there are args, and run them and then enter interactive
            if (args.length != 0) {
                gshell.execute(args);
            }

            interp.run();
        }
        else {
            status = gshell.execute(args);
        }

        if (debug) {
            log.debug("Ran for " + watch);
        }

        System.exit(status);
    }

    //
    // Bootstrap
    //

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
