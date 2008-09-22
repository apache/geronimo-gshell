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
 * The default {@link CommandDocumenter} component.
 *
 * @version $Rev$ $Date$
 */
public class CommandDocumenterImpl
    implements CommandDocumenter, CommandAware
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Command command;

    private String name;

    private String description;

    private String manual;

    private Renderer renderer = new Renderer();

    public String getName() {
        if (name == null) {
            name = getCommand().getMessages().getMessage("command.name");
        }
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        if (description == null) {
            description = getCommand().getMessages().getMessage("command.description");
        }
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getManual() {
        if (manual == null) {
            manual = getCommand().getMessages().getMessage("command.manual");
        }
        return manual;
    }

    public void setManual(final String manual) {
        this.manual = manual;
    }

    //
    // CommandAware
    //

    public void setCommand(final Command command) {
        assert command != null;

        this.command = command;
    }

    private Command getCommand() {
        assert command != null;

        return command;
    }

    //
    // CommandDocumenter
    //

    public void renderUsage(final PrintWriter out) {
        assert out != null;

        log.debug("Rendering command usage");
        
        CommandLineProcessor clp = new CommandLineProcessor();
        
        // Attach our helper to inject --help
        CommandImpl.HelpSupport help = new CommandImpl.HelpSupport();
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

    public void renderManual(final PrintWriter out) {
        assert out != null;

        log.debug("Rendering command manual");

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