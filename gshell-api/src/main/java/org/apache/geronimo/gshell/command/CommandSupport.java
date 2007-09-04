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

package org.apache.geronimo.gshell.command;

import java.util.Iterator;

import org.apache.geronimo.gshell.clp.CommandLineProcessor;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.clp.Printer;
import org.apache.geronimo.gshell.IO;
import org.apache.geronimo.gshell.common.Arguments;
import org.apache.geronimo.gshell.common.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides support for {@link Command} implemenations.
 *
 * @version $Rev$ $Date$
 */
public abstract class CommandSupport
    implements Command
{
    protected Logger log;

    private String name;

    private CommandContext context;

    @Option(name="-h", aliases={"--help"}, description="Display this help message")
    private boolean displayHelp;

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
        assert name != null;
        
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
        log = LoggerFactory.getLogger(this.getClass().getName() + "." + getName());

        log.debug("Initializing");

        this.context = context;

        //
        // TODO: Add preference support
        //
        
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

        Object result = null;

        try {
            CommandLineProcessor clp = new CommandLineProcessor(this);
            clp.process(Arguments.toStringArray(args));

            //
            // TODO: Need to mark this option as superceeding other required arguments/options
            //
            
            // Handle --help/-h automatically for the command
            if (displayHelp) {
                displayHelp(clp);
            }
            else {
                // Invoke the command's action
                result = doExecute();
            }
        }
        catch (Exception e) {
            log.error(e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Exception details", e);
            }

            result = Command.FAILURE;
        }
        catch (Notification n) {
            // Always re-throw notifications
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
     */
    protected abstract Object doExecute() throws Exception;

    //
    // CLI Fluff
    //

    /**
     * Returns the command-line usage.
     *
     * @return  The command-line usage.
     */
    protected String getUsage() {
        return "[options]";
    }

    protected void displayHelp(final CommandLineProcessor clp) {
        assert clp != null;

        IO io = getIO();

        io.out.print(getName());
        io.out.print(" -- ");
        io.out.println();

        Printer printer = new Printer(clp);
        printer.printUsage(io.out);
        io.out.println();
    }
}
