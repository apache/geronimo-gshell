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
import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.util.Arguments;
import org.apache.geronimo.gshell.ExitNotification;

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

    protected CommandSupport() {
        // Sub-class must call setName()
    }

    public void setName(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name is null");
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

    public final void init(final CommandContext context) {
        if (this.context != null) {
            throw new IllegalStateException("Command already initalized");
        }

        // Initialize logging with command name
        log = LogFactory.getLog(this.getClass().getName() + "." + getName());

        log.debug("Initializing");

        this.context = context;

        doInit();
    }

    protected void doInit() {
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

        doDestroy();

        this.context = null;
    }

    protected void doDestroy() {
        // Sub-class should override to provide custom cleanup
    }

    public void abort() {
        // Sub-calss should override to allow for custom abort functionality
    }

    //
    // Context Helpers
    //

    protected CommandContext getCommandContext() {
        assert context != null;

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

    public int execute(final String... args) throws Exception {
        assert args != null;

        // Make sure that we have been initialized before we go any further
        ensureInitialized();

        log.info("Executing w/arguments: " + Arguments.asString(args));

        int status;

        try {
            status = doExecute(args);
        }
        catch (Exception e) {
            log.error(e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Exception details", e);
            }

            status = Command.FAILURE;
        }
        catch (ExitNotification n) {
            //
            // HACK: Propagate the notifciation
            //

            throw n;
        }
        catch (Error e) {
            log.error(e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Error details", e);
            }

            status = Command.FAILURE;
        }
        finally {
            // Be sure to flush the commands outputs
            getIO().flush();
        }

        log.info("Command exited with status code: " + status);

        return status;
    }

    protected int doExecute(final String[] args) throws Exception {
        // Sub-class should override to perform custom execution

        return Command.FAILURE;
    }
}
