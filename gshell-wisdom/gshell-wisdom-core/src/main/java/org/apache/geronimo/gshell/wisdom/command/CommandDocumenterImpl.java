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

import org.apache.geronimo.gshell.clp.CommandLineProcessor;
import org.apache.geronimo.gshell.clp.Printer;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandDocumenter;
import org.apache.geronimo.gshell.command.CommandInfo;

import javax.annotation.PostConstruct;
import java.io.PrintWriter;

/**
 * The default {@link CommandDocumenter} component.
 *
 * @version $Rev$ $Date$
 */
public class CommandDocumenterImpl
    extends CommandContainerComponentSupport
    implements CommandDocumenter
{
    //
    // TODO: Need to provide i18n keys to lookup the values for name & description
    //
    
    private String name;

    private String description;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @PostConstruct
    public void init() {
        // TODO: Validate
    }
    
    // CommandDocumenter

    public void renderUsage(final CommandInfo info, final PrintWriter out) {
        assert info != null;
        assert out != null;

        CommandLineProcessor clp = new CommandLineProcessor();

        // Attach our helper to inject --help
        CommandContainerImpl.HelpSupport help = new CommandContainerImpl.HelpSupport();
        clp.addBean(help);

        // And then the beans options
        CommandAction action = getContainer().getAction();
        clp.addBean(action);

        // Fetch the details
        String name = getName();
        String desc = getDescription();

        //
        // TODO: Add some nice ANSI muck
        //

        // Render the help
        out.println(desc);
        out.println();

        Printer printer = new Printer(clp);

        printer.printUsage(out, name);
        out.println();
    }

    public void renderManual(final CommandInfo info, final PrintWriter out) {
        assert info != null;
        assert out != null;

        out.println(info.getName());
        out.println();
        out.println("TODO: Full docs");
    }
}