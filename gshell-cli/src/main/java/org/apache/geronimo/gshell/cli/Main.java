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

package org.apache.geronimo.gshell.cli;

import java.util.List;

import jline.Terminal;
import org.apache.geronimo.gshell.InteractiveShell;
import org.apache.geronimo.gshell.Shell;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.CommandLineProcessor;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.clp.Printer;
import org.apache.geronimo.gshell.console.Console;
import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.console.JLineConsole;
import org.apache.geronimo.gshell.util.Banner;
import org.apache.geronimo.gshell.util.Version;
import org.codehaus.classworlds.ClassWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command-line interface to bootstrap Shell.
 *
 * @version $Rev$ $Date$
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

    // FIXME
    // private final StopWatch watch = new StopWatch();

    public Main(final ClassWorld world) {
        assert world != null;
        this.world = world;

        // FIXME:
        // watch.start();
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

    @Option(name="-h", aliases={"--help"}, description="Display this help message")
    private boolean help;

    @Option(name="-V", aliases={"--version"}, description="Display GShell version")
    private boolean version;

    @Option(name="-i", aliases={"--interactive"}, argumentRequired=false, description="Run in interactive mode")
    private boolean interactive = true;

    @Option(name="-debug", aliases={"--debug"}, description="Enable DEBUG logging output")
    private boolean debug;

    @Option(name="-verbose", aliases={"--verbose"}, description="Enable INFO logging output")
    private boolean verbose;

    @Option(name="-quite", aliases={"--quiet"}, description="Limit logging output to ERROR")
    private boolean quiet;

    @Option(name="-c", aliases={"commands"}, description="Read commands from string")
    private String commands;

    @Argument
    private List<String> args;


    //
    // TODO: Change this to --interactive false
    //

    @Option(name="-n", aliases={"--non-interactive"}, description="Run in non-interactive mode")
    private boolean nonInteractive;

    public void run(final String[] args) throws Exception {
        assert args != null;

        /*
        options.addOption(OptionBuilder.withLongOpt("define")
            .withDescription("Define a system property")
            .hasArg()
            .withArgName("name=value")
            .create('D'));
        */

        CommandLineProcessor clp = new CommandLineProcessor(this);
        clp.setStopAtNonOption(true);
        clp.process(args);

        if (help) {
            io.out.println(Banner.getBanner());

            io.out.println();
            io.out.println(System.getProperty("program.name", "gshell") + " [options] <command> [args]");
            io.out.println();

            Printer printer = new Printer(clp);
            printer.printUsage(io.out);

            io.out.println();
            io.out.flush();

            System.exit(0);
        }

        if (version) {
            io.out.println(Banner.getBanner());
            io.out.println(Version.getInstance());
            io.out.println();
            io.out.flush();

            System.exit(0);
        }

        /*
        if (line.hasOption('D')) {
            String[] values = line.getOptionValues('D');

            for (String value : values) {
                setPropertyFrom(value);
            }
        }
        */

        if (quiet) {
            setConsoleLogLevel("ERROR");
        }
        else if (debug) {
            setConsoleLogLevel("DEBUG");
        }
        else if (verbose) {
            setConsoleLogLevel("INFO");
        }
        else {
            // Default is to be quiet
            setConsoleLogLevel("WARN");
        }

        int code;
        
        try {
            if (this.args == null || this.args.size() == 0) {
                code = execute(new String[0]);
            }
            else {
                code = execute(this.args.toArray(new String[this.args.size()]));
            }
        }
        finally {
            io.flush();
        }
        
        System.exit(code);
    }

    private int execute(final String[] args) throws Exception {
        // Its okay to use logging now
        Logger log = LoggerFactory.getLogger(Main.class);
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

        // FIXME:
        /*
        if (debug) {
            log.debug("Started in " + watch);
        }
        */

        Object result = null;

        //
        // TODO: Pass interactive flags (maybe as property) so gshell knows what mode it is
        //

        if (commands != null) {
            gshell.execute(commands);
        }
        else if (interactive) {
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
            result = gshell.execute(args);
        }

        // FIXME:
        /*
        if (debug) {
            log.debug("Ran for " + watch);
        }
        */
        
        // If the result is a number, then pass that back to the calling shell
        int code = 0;
        
        if (result instanceof Number) {
            code = ((Number)result).intValue();
        }
        
        if (debug) {
            log.debug("Exiting with code: " + code);
        }
        
        return code;
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
