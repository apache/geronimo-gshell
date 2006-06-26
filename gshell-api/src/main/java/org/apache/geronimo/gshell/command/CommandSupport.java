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

package org.apache.geronimo.gshell.command;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.CommandLine;
import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.util.Arguments;

import java.util.Iterator;

/**
 * Provides support for {@link Command} implemenations.
 *
 * @version $Id$
 */
public abstract class CommandSupport
    implements Command
{
    protected Log log;

    private String name;

    private CommandContext context;

    protected CommandSupport(final String name) {
        setName(name);
    }

    /**
     * Sub-class <b>must</b> call {@link #setName(String)}.
     */
    protected CommandSupport() {
        super();
    }

    public void setName(final String name) {
        if (name == null) {
            throw new NullArgumentException("name");
        }
        if (name.trim().length() == 0) {
            throw new IllegalArgumentException("Name is empty");
        }

        this.name = name;
    }

    public String getName() {
        if (name == null) {
            throw new IllegalStateException("Name was not set");
        }

        return name;
    }

    //
    // Life-cycle
    //

    private void dump(final Variables vars) {
        Iterator<String> iter = vars.names();

        if (iter.hasNext()) {
            log.debug("Variables:");
        }

        while (iter.hasNext()) {
            String name = iter.next();

            log.debug("    " + name + "=" + vars.get(name));
        }
    }

    public final void init(final CommandContext context) {
        if (this.context != null) {
            throw new IllegalStateException("Command already initalized");
        }

        // Initialize logging with command name
        log = LogFactory.getLog(this.getClass().getName() + "." + getName());

        log.debug("Initializing");

        this.context = context;

        if (log.isDebugEnabled()) {
            dump(context.getVariables());
        }

        try {
            doInit();
        }
        catch (Exception e) {
            log.error("Initialization failed", e);

            //
            // HACK: Need to handle or descide to ignore this exception
            //

            throw new RuntimeException("Command initialization failed", e);
        }

        log.debug("Initialized");
    }

    protected void doInit() throws Exception {
        // Sub-class should override to provide custom initialization
    }

    private void ensureInitialized() {
        if (context == null) {
            throw new IllegalStateException("Command has not been initialized");
        }
    }

    public final void destroy() {
        if (this.context == null) {
            throw new IllegalStateException("Command already destroyed (or never initialized)");
        }

        log.debug("Destroying");

        if (log.isDebugEnabled()) {
            dump(context.getVariables());
        }

        try {
            doDestroy();
        }
        catch (Exception e) {
            log.error("Destruction failed", e);

            //
            // HACK: Need to handle or descide to ignore this exception
            //

            throw new RuntimeException("Command destruction failed", e);
        }

        this.context = null;

        log.debug("Destroyed");
    }

    protected void doDestroy() throws Exception {
        // Sub-class should override to provide custom cleanup
    }

    public void abort() {
        // Sub-calss should override to allow for custom abort functionality
    }

    //
    // Context Helpers
    //

    protected CommandContext getCommandContext() {
        if (context == null) {
            throw new IllegalStateException("Not initialized; missing command context");
        }

        return context;
    }

    protected Variables getVariables() {
        return getCommandContext().getVariables();
    }

    protected IO getIO() {
        return getCommandContext().getIO();
    }

    protected MessageSource getMessageSource() {
        return getCommandContext().getMessageSource();
    }

    //
    // Execute Helpers
    //

    public Object execute(final Object... args) throws Exception {
        assert args != null;

        // Make sure that we have been initialized before we go any further
        ensureInitialized();

        boolean info = log.isInfoEnabled();

        if (info) {
            log.info("Executing w/arguments: " + Arguments.asString(args));
        }

        Object result;

        try {
            // Handle the command-line
            Options options = getOptions();
            CommandLineParser parser = new PosixParser();
            CommandLine line = parser.parse(options, Arguments.toStringArray(args));

            // Custom command-line processing
            boolean usage = processCommandLine(line);

            // Default command-line processing
            if (usage || line.hasOption('h')) {
                displayHelp(options);

                return Command.SUCCESS;
            }

            // Execute with the remaining arguments post-processing
            result = doExecute(line.getArgs());
        }
        catch (Exception e) {
            log.error(e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Exception details", e);
            }

            result = Command.FAILURE;
        }
        catch (Notification n) {
            //
            // Always rethrow notifications
            //
            throw n;
        }
        catch (Error e) {
            log.error(e.getMessage());

            if (log.isDebugEnabled()) {
                log.debug("Error details", e);
            }

            result = Command.FAILURE;
        }
        finally {
            // Be sure to flush the commands outputs
            getIO().flush();
        }

        if (info) {
            log.info("Command exited with result: " + result);
        }

        return result;
    }

    /**
     * Sub-class should override to perform custom execution.
     *
     * @param args  Post-command-line parsed options.
     * @return
     *
     * @throws Exception
     */
    protected abstract Object doExecute(final Object[] args) throws Exception;

    //
    // CLI Fluff
    //

    /**
     * Process the command-line.
     *
     * <p>
     * The default is to provide no additional processing.
     *
     * @param line  The command-line to process.
     * @return      True to display help and exit; else false to continue.
     *
     * @throws CommandException
     */
    protected boolean processCommandLine(final CommandLine line) throws CommandException {
        return false;
    }

    /**
     * Get the command-line options to process.
     *
     * <p>
     * The default is to provide --help and -h support.
     *
     * @return  The command-line options; never null;
     */
    protected Options getOptions() {
        MessageSource messages = getMessageSource();

        Options options = new Options();

        options.addOption(OptionBuilder.withLongOpt("help")
            .withDescription(messages.getMessage("cli.option.help"))
            .create('h'));

        return options;
    }

    /**
     * Returns the command-line usage.
     *
     * @return  The command-line usage.
     */
    protected String getUsage() {
        return "[options]";
    }

    protected void displayHelp(final Options options) {
        MessageSource messages = getMessageSource();
        IO io = getIO();

        io.out.print(getName());
        io.out.print(" -- ");
        io.out.println(messages.getMessage("cli.usage.description"));
        io.out.println();

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(
            io.out,
            80, // width (FIXME: Should pull from gshell.columns variable)
            getName() + " " + getUsage(),
            "",
            options,
            4, // left pad
            4, // desc pad
            "",
            false); // auto usage

        io.out.println();

        String footer = messages.getMessage("cli.usage.footer");
        if (footer.trim().length() != 0) {
            io.out.println(footer);
            io.out.println();
        }
    }
}
