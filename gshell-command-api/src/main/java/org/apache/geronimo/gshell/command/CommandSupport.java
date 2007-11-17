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

import org.apache.geronimo.gshell.clp.CommandLineProcessor;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.clp.Printer;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.common.Arguments;
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
    protected Logger log = LoggerFactory.getLogger(getClass());

    protected CommandContext context;

    protected IO io;

    protected Variables variables;
    
    @Option(name="-h", aliases={"--help"}, description="Display this help message", requireOverride=true)
    private boolean displayHelp;

    @Deprecated
    public String getId() {
        CommandComponent cmd = getClass().getAnnotation(CommandComponent.class);
        if (cmd == null) {
            throw new IllegalStateException("Command id not found");
        }
        return cmd.id();
    }

    @Deprecated
    public String getDescription() {
        CommandComponent cmd = getClass().getAnnotation(CommandComponent.class);
        if (cmd == null) {
            throw new IllegalStateException("Command description not found");
        }
        return cmd.description();
    }

    public void init(final CommandContext context) {
        assert context != null;

        this.context = context;
        this.io = context.getIO();
        this.variables = context.getVariables();

        // Re-setup logging using our id
        String id = getId();
        log = LoggerFactory.getLogger(getClass().getName() + "." + id);
    }

    public Object execute(final CommandContext context, final Object... args) throws Exception {
        assert context != null;
        assert args != null;

        init(context);

        log.info("Executing w/args: [{}]", Arguments.asString(args));

        CommandLineProcessor clp = new CommandLineProcessor(this);
        clp.process(Arguments.toStringArray(args));

        // Handle --help/-h automatically for the command
        if (displayHelp) {
            //
            // TODO: Make a special PrinterHandler to abstract this muck from having to process it by hand
            //
            
            displayHelp(context, clp);
            
            return SUCCESS;
        }

        return doExecute();
    }

    protected abstract Object doExecute() throws Exception;

    protected void displayHelp(final CommandContext context, final CommandLineProcessor clp) {
        assert context != null;
        assert clp != null;

        // Use the alias if we have one, else use the command name
        String name = context.getInfo().getAlias();
        if (name == null) {
            name = context.getInfo().getName();
        }

        //
        // FIXME: This is uuuuuggggllyyyy
        //
        
        io.out.println(name);
        io.out.println(" -- ");
        io.out.println();

        Printer printer = new Printer(clp);
        printer.printUsage(io.out);
        io.out.println();
    }
}
