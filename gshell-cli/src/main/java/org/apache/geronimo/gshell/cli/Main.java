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

import java.util.ArrayList;
import java.util.List;

import jline.Terminal;
import org.apache.geronimo.gshell.ExitNotification;
import org.apache.geronimo.gshell.JLineConsole;
import org.apache.geronimo.gshell.Shell;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.CommandLineProcessor;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.clp.Printer;
import org.apache.geronimo.gshell.common.StopWatch;
import org.apache.geronimo.gshell.console.Console;
import org.apache.geronimo.gshell.console.IO;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;

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

    private final ClassWorld classWorld;

    private final IO io = new IO();

    private final StopWatch watch = new StopWatch();

    public Main(final ClassWorld classWorld) {
        assert classWorld != null;

        this.classWorld = classWorld;

        watch.start();
    }


    @Option(name="-h", aliases={"--help"}, description="Display this help message")
    private boolean help;

    @Option(name="-V", aliases={"--version"}, description="Display GShell version")
    private boolean version;

    @Option(name="-i", aliases={"--interactive"}, description="Run in interactive mode")
    private boolean interactive = true;

    private void setConsoleLogLevel(final String level) {
        System.setProperty("gshell.log.console.level", level);
    }

    @Option(name="-d", aliases={"--debug"}, description="Enable DEBUG logging output")
    private void setDebug(boolean flag) {
        if (flag) {
            setConsoleLogLevel("DEBUG");
        }
    }

    @Option(name="-v", aliases={"--verbose"}, description="Enable INFO logging output")
    private void setVerbose(boolean flag) {
        if (flag) {
            setConsoleLogLevel("INFO");
        }
    }

    @Option(name="-q", aliases={"--quiet"}, description="Limit logging output to ERROR")
    private void setQuiet(boolean flag) {
        if (flag) {
            setConsoleLogLevel("ERROR");
        }
    }

    @Option(name="-c", aliases={"--commands"}, description="Read commands from string")
    private String commands;

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    @Argument(description="Command")
    private List<String> args = new ArrayList<String>(0);

    @Option(name="-D", aliases={"--define"}, metaVar="NAME=VALUE", description="Define system properties")
    private void setSystemProperty(final String nameValue) {
        assert nameValue != null;

        String name, value;
        int i = nameValue.indexOf("=");

        if (i == -1) {
            name = nameValue;
            value = Boolean.TRUE.toString();
        }
        else {
            name = nameValue.substring(0, i);
            value = nameValue.substring(i + 1, nameValue.length());
        }
        name = name.trim();

        System.setProperty(name, value);
    }

    public void run(final String[] args) throws Exception {
        assert args != null;

        // Default is to be quiet
        setConsoleLogLevel("WARN");
        
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

        int code;
        
        try {
            code = execute(this.args.toArray(new String[this.args.size()]));
        }
        finally {
            io.flush();
        }
        
        System.exit(code);
    }

    private int execute(final String[] args) throws Exception {
        // Its okay to use logging now
        final Logger log = LoggerFactory.getLogger(Main.class);

        // Boot up the container
        ContainerConfiguration config = new DefaultContainerConfiguration();
        config.setName("gshell.core");
        config.setClassWorld(classWorld);

        PlexusContainer container = new DefaultPlexusContainer(config);

        System.err.println("Logger manager: " + container.getLoggerManager());

        //
        // TODO: We need to pass in our I/O context to the container directly
        //
        
        // Load the GShell instance
        final Shell shell = (Shell) container.lookup(Shell.class);

        //
        // TEMP: Log some info about the terminal
        //

        Terminal term = Terminal.getTerminal();

        log.debug("Using terminal: {}", term);
        log.debug("  Supported: {}", term.isSupported());
        log.debug("  H x W: {} x {}", term.getTerminalHeight(), term.getTerminalWidth());
        log.debug("  Echo: {}", term.getEcho());
        log.debug("  ANSI: {} ", term.isANSISupported());

        log.debug("Started in {}", watch);

        Object result = null;

        //
        // TODO: Pass interactive flags (maybe as property) so gshell knows what modfooe it is
        //

        if (commands != null) {
            shell.execute(commands);
        }
        else if (interactive) {
            log.debug("Starting interactive console");

            Console.Executor executor = new Console.Executor() {
                public Result execute(String line) throws Exception {
                    try {
                        Object result = shell.execute(line);
                    }
                    catch (ExitNotification n) {
                        return Result.STOP;
                    }

                    return Result.CONTINUE;
                }
            };

            JLineConsole runner = new JLineConsole(executor, shell);

            runner.setErrorHandler(new Console.ErrorHandler() {
                public Result handleError(Throwable error) {
                    log.error("Execution failed: " + error, error);
                    
                    return Result.CONTINUE;
                }
            });

            // Check if there are args, and run them and then enter interactive
            if (args.length != 0) {
                shell.execute(args);
            }

            runner.run();
        }
        else {
            result = shell.execute(args);
        }

        log.debug("Ran for {}", watch);

        // If the result is a number, then pass that back to the calling shell
        int code = 0;
        
        if (result instanceof Number) {
            code = ((Number)result).intValue();
        }

        log.debug("Exiting with code: {}", code);

        return code;
    }

    //
    // Bootstrap
    //

    public static void main(final String[] args, final ClassWorld world) throws Exception {
        Main main = new Main(world);
        main.run(args);
    }

    public static void main(final String[] args) throws Exception {
        main(args, new ClassWorld("gshell.legacy", Thread.currentThread().getContextClassLoader()));
    }
}
