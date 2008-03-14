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
import java.util.concurrent.atomic.AtomicReference;

import org.apache.geronimo.gshell.ExitNotification;
import org.apache.geronimo.gshell.GShell;
import org.apache.geronimo.gshell.ansi.ANSI;
import org.apache.geronimo.gshell.branding.Branding;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.CommandLineProcessor;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.clp.Printer;
import org.apache.geronimo.gshell.command.IO;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;

/**
 * Command-line bootstrap for GShell.
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

    public Main(final ClassWorld classWorld) {
        assert classWorld != null;

        this.classWorld = classWorld;
    }
    
    //
    // TODO: Add flag to capture output to log file
    //       https://issues.apache.org/jira/browse/GSHELL-47
    //

    @Option(name="-h", aliases={"--help"}, requireOverride=true, description="Display this help message")
    private boolean help;

    @Option(name="-V", aliases={"--version"}, requireOverride=true, description="Display program version")
    private boolean version;

    @Option(name="-i", aliases={"--interactive"}, description="Run in interactive mode")
    private boolean interactive = true;

    private void setConsoleLogLevel(final String level) {
        System.setProperty("gshell.log.console.level", level);
    }

    @Option(name="-e", aliases={"--exception"}, description="Enable exception stack traces")
    private void setException(boolean flag) {
    	if (flag) {
    		System.setProperty("gshell.show.stacktrace","true");
    	}
    }
    
    @Option(name="-d", aliases={"--debug"}, description="Enable DEBUG logging output")
    private void setDebug(boolean flag) {
        if (flag) {
            setConsoleLogLevel("DEBUG");
            io.setVerbosity(IO.Verbosity.DEBUG);
        }
    }

    @Option(name="-v", aliases={"--verbose"}, description="Enable INFO logging output")
    private void setVerbose(boolean flag) {
        if (flag) {
            setConsoleLogLevel("INFO");
            io.setVerbosity(IO.Verbosity.VERBOSE);
        }
    }                                                 

    @Option(name="-q", aliases={"--quiet"}, description="Limit logging output to ERROR")
    private void setQuiet(boolean flag) {
        if (flag) {
            setConsoleLogLevel("ERROR");
            io.setVerbosity(IO.Verbosity.QUIET);
        }
    }

    @Option(name="-c", aliases={"--commands"}, description="Read commands from string")
    private String commands;

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    @Argument(description="Command")
    private List<String> commandArgs = new ArrayList<String>(0);

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

    @Option(name="-C", aliases={"--color"}, argumentRequired=true, description="Enable or disable use of ANSI colors")
    private void enableAnsiColors(final boolean flag) {
        ANSI.setEnabled(flag);
    }

    @Option(name="-T", aliases={"--terminal"}, metaVar="TYPE", argumentRequired=true, description="Specify the terminal TYPE to use")
    private void setTerminalType(String type) {
        type = type.toLowerCase();

        if ("unix".equals(type)) {
            type = "jline.UnixTerminal";
        }
        else if ("win".equals(type) || "windows".equals("type")) {
            type = "jline.WindowsTerminal";
        }
        else if ("false".equals(type) || "off".equals(type) || "none".equals(type)) {
            type = "jline.UnsupportedTerminal";
            
            //
            // HACK: Disable ANSI, for some reason UnsupportedTerminal reports ANSI as enabled, when it shouldn't
            //
            ANSI.setEnabled(false);
        }

        System.setProperty("jline.terminal", type);
    }

    private PlexusContainer createContainer() throws PlexusContainerException {
        // Boot up the container
        ContainerConfiguration config = new DefaultContainerConfiguration();
        config.setName("gshell.core");
        config.setClassWorld(classWorld);

        return new DefaultPlexusContainer(config);
    }

    public void boot(final String[] args) throws Exception {
        assert args != null;

        // Default is to be quiet
        setConsoleLogLevel("WARN");

        CommandLineProcessor clp = new CommandLineProcessor(this);
        clp.setStopAtNonOption(true);
        clp.process(args);

        //
        // HACK: We need the dang branding...
        //

        Branding branding = null;
        if (help || version) {
            PlexusContainer container = createContainer();
            branding = (Branding) container.lookup(Branding.class);
        }

        //
        // TODO: Use methods to handle these...
        //

        if (help) {
            io.out.println(branding.getProgramName() + " [options] <command> [args]");
            io.out.println();

            Printer printer = new Printer(clp);
            printer.printUsage(io.out);

            io.out.println();
            io.out.flush();

            System.exit(ExitNotification.DEFAULT_CODE);
        }

        if (version) {
            io.out.println(branding.getVersion());
            io.out.println();
            io.out.flush();

            System.exit(ExitNotification.DEFAULT_CODE);
        }

        // Setup a refereence for our exit code so our callback thread can tell if we've shutdown normally or not
        final AtomicReference<Integer> codeRef = new AtomicReference<Integer>();
        int code = ExitNotification.DEFAULT_CODE;

        Runtime.getRuntime().addShutdownHook(new Thread("GShell Shutdown Hook") {
            public void run() {
                if (codeRef.get() == null) {
                    // Give the user a warning when the JVM shutdown abnormally, normal shutdown
                    // will set an exit code through the proper channels

                    io.err.println();
                    io.err.println("WARNING: Abnormal JVM shutdown detected");
                }

                io.flush();
            }
        });

        try {
            GShell gshell = new GShell(classWorld, io);

            // clp gives us a list, but we need an array
            String[] _args = commandArgs.toArray(new String[commandArgs.size()]);

            if (commands != null) {
                gshell.execute(commands);
            }
            else if (interactive) {
                gshell.run(_args);
            }
            else {
                gshell.execute(_args);
            }
        }
        catch (ExitNotification n) {
            code = n.code;
        }

        codeRef.set(code);

        System.exit(code);
    }

    public static void main(final String[] args, final ClassWorld world) throws Exception {
        Main main = new Main(world);
        main.boot(args);
    }

    public static void main(final String[] args) throws Exception {
        main(args, new ClassWorld("gshell", Thread.currentThread().getContextClassLoader()));
    }
}

