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

package org.apache.geronimo.gshell.rapture;

import org.apache.geronimo.gshell.clp.CommandLineProcessor;
import org.apache.geronimo.gshell.clp.Printer;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandDocumenter;
import org.apache.geronimo.gshell.command.CommandInfo;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.plexus.GShellPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

/**
 * The default {@link CommandDocumenter} component.
 *
 * @version $Rev$ $Date$
 */
@Component(role=CommandDocumenter.class)
public class DefaultCommandDocumenter
    implements CommandDocumenter, Contextualizable
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private GShellPlexusContainer container;

    // Contextualizable

    public void contextualize(final Context context) throws ContextException {
        assert context != null;

        container = (GShellPlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
        assert container != null;

        log.debug("Container: {}", container);
    }

    // CommandDocumenter
    
    public String getName(final CommandInfo info) {
        assert info != null;

        // Use the alias if we have one, else use the command name
        String name = info.getAlias();
        if (name == null) {
            name = info.getName();
        }

        return name;
    }

    public String getDescription(final CommandInfo info) {
        assert info != null;

        //
        // HACK: This needs to chagne, doing this for now to make it work
        //

        CommandAction action = getAction(info);
        CommandComponent cmd = action.getClass().getAnnotation(CommandComponent.class);
        if (cmd == null) {
            throw new IllegalStateException("Command description not found");
        }

        return cmd.description();
    }

    /**
     * Get the action instance for the given command context.
     */
    private CommandAction getAction(final CommandInfo info) {
        assert info != null;
        assert container != null;

        try {
            return container.lookupComponent(CommandAction.class, info.getId());
        }
        catch (ComponentLookupException e) {
            throw new RuntimeException(e);
        }
    }

    public void renderUsage(final CommandInfo info, final PrintWriter out) {
        assert info != null;
        assert out != null;

        CommandLineProcessor clp = new CommandLineProcessor();

        // Attach our helper to inject --help
        DefaultCommand.HelpSupport help = new DefaultCommand.HelpSupport();
        clp.addBean(help);

        // And then the beans options
        CommandAction action = getAction(info);
        clp.addBean(action);

        // Fetch the details
        String name = getName(info);
        String desc = getDescription(info);

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

        // TODO: Render a more complete manual for the command, maybe using simple APT-like syntax.
    }
}