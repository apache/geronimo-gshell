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

package org.apache.geronimo.gshell.plugin;

import org.apache.geronimo.gshell.command.CommandContainer;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.Executable;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.plexus.GShellPlexusContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.PlexusConstants;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
@Component(role=CommandContainer.class)
public class DefaultCommandContainer
    implements CommandContainer, Contextualizable
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private GShellPlexusContainer container;

    @Configuration("invalid")
    private String commandId;

    // Contextualizable
    
    public void contextualize(final Context context) throws ContextException {
        assert context != null;

        container = (GShellPlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
        assert container != null;
        
        log.debug("Container: {}", container);
    }

    // CommandContainer

    public String getId() {
        return commandId;
    }

    public String getDescription() {
        //
        // FIXME:
        //

        CommandComponent cmd = getExecutable().getClass().getAnnotation(CommandComponent.class);
        if (cmd == null) {
            throw new IllegalStateException("Command description not found");
        }
        return cmd.description();
    }

    public Executable getExecutable() {
        assert container != null;

        try {
            return container.lookupComponent(Command.class, commandId);
        }
        catch (ComponentLookupException e) {
            throw new RuntimeException(e);
        }
    }

    public Object execute(final CommandContext context, final Object... args) throws Exception {
        assert context != null;
        assert args != null;

        log.trace("Executing; context={}, args={}", context, args);

        Executable executable = getExecutable();

        // TODO: Handle logging muck
        
        // TODO: Bind context, io and variables

        // TODO: Process CLP

        Object result = executable.execute(context, args);

        log.trace("Result: {}", result);

        return result;
    }
}