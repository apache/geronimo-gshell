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

package org.apache.geronimo.gshell.wisdom.command;

import org.apache.geronimo.gshell.ansi.Code;
import org.apache.geronimo.gshell.ansi.Renderer;
import org.apache.geronimo.gshell.clp.CommandLineProcessor;
import org.apache.geronimo.gshell.clp.Printer;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandAware;
import org.apache.geronimo.gshell.command.CommandDocumenter;
import org.apache.geronimo.gshell.i18n.PrefixingMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

/**
 * Support for {@link CommandDocumenter} components.
 *
 * @version $Rev$ $Date$
 */
public abstract class CommandDocumenterSupport
    implements CommandDocumenter, CommandAware
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Command command;

    //
    // CommandAware
    //

    public void setCommand(final Command command) {
        assert command != null;

        this.command = command;
    }

    protected Command getCommand() {
        assert command != null;

        return command;
    }

    //
    // CommandDocumenter
    //

    public void renderUsage(final PrintWriter out) {
        assert out != null;

        log.trace("Rendering command usage");

        CommandLineProcessor clp = new CommandLineProcessor();

        // Attach our helper to inject --help
        HelpSupport help = new HelpSupport();
        clp.addBean(help);

        // And then the beans options
        CommandAction action = getCommand().getAction();
        clp.addBean(action);

        // Render the help
        out.println(getDescription());
        out.println();

        Printer printer = new Printer(clp);
        printer.setMessageSource(new PrefixingMessageSource(getCommand().getMessages(), "command."));
        printer.printUsage(out, getName());
    }

    protected abstract String getManual();
    
    public void renderManual(final PrintWriter out) {
        assert out != null;

        log.trace("Rendering command manual");

        Renderer renderer = new Renderer();
        
        out.println(renderer.render(Renderer.encode("NAME", Code.BOLD)));
        out.print("  ");
        out.println(getName());
        out.println();

        out.println(renderer.render(Renderer.encode("DESCRIPTION", Code.BOLD)));
        out.print("  ");
        out.println(getDescription());
        out.println();

        out.println(renderer.render(Renderer.encode("MANUAL", Code.BOLD)));
        out.println(getManual());
        out.println();
    }
}