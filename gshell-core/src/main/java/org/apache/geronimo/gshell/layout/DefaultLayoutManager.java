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

package org.apache.geronimo.gshell.layout;

import java.io.IOException;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.layout.loader.LayoutLoader;
import org.apache.geronimo.gshell.layout.model.AliasNode;
import org.apache.geronimo.gshell.layout.model.CommandNode;
import org.apache.geronimo.gshell.layout.model.GroupNode;
import org.apache.geronimo.gshell.layout.model.Layout;
import org.apache.geronimo.gshell.layout.model.Node;
import org.apache.geronimo.gshell.registry.CommandRegistry;
import org.apache.geronimo.gshell.registry.NotRegisteredException;
import org.apache.geronimo.gshell.shell.Environment;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of the {@link LayoutManager}.
 *
 * @version $Rev$ $Date$
 */
@Component(role=LayoutManager.class)
public class DefaultLayoutManager
    implements LayoutManager, Initializable
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private CommandRegistry commandRegistry;

    @Requirement
    private LayoutLoader loader;

    @Requirement
    private Environment env;
    
    private Layout layout;

    public DefaultLayoutManager() {}
    
    public DefaultLayoutManager(final CommandRegistry commandRegistry, final LayoutLoader loader, final Environment env) {
        this.commandRegistry = commandRegistry;
        this.loader = loader;
        this.env = env;
    }

    public DefaultLayoutManager(final CommandRegistry commandRegistry, final Layout layout, final Environment env) {
        this.commandRegistry = commandRegistry;
        this.layout = layout;
        this.env = env;
    }

    public void initialize() throws InitializationException {
        assert loader != null;

        try {
            layout = loader.load();
        }
        catch (IOException e) {
            throw new InitializationException(e.getMessage(), e);
        }
    }

    public Layout getLayout() {
        return layout;
    }

    public Command find(final String path) throws NotFoundException {
        assert path != null;

        log.debug("Searching for command: {}", path);

        Node start;

        if (path.startsWith("/")) {
            start = layout;
        }
        else {
            //
            // FIXME: Use a FQN for this and expose as static final
            //

            start = (Node) env.getVariables().get("CURRENT_NODE");

            if (start == null) {
                start = layout;
            }
        }

        String id = findCommandId(start, path);

        try {
            return commandRegistry.lookup(id);
        }
        catch (NotRegisteredException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    private String findCommandId(final Node start, final String path) throws NotFoundException {
        assert start != null;
        assert path != null;

        Node node = findNode(start, path);

        if (node instanceof CommandNode) {
            return ((CommandNode)node).getId();
        }
        else if (node instanceof AliasNode) {
            String cmd = ((AliasNode)node).getCommand();

            return findCommandId(layout, cmd);
        }

        throw new NotFoundException(path);
    }

    private Node findNode(final Node start, final String path) throws NotFoundException {
        assert start != null;
        assert path != null;

        Node current = start;

        String[] elements = path.split("/");
        
        for (String element : elements) {
            if (current instanceof GroupNode) {
                Node node = ((GroupNode)current).find(element);

                if (node == null) {
                    throw new NotFoundException(path);
                }

                current = node;
            }
            else {
                throw new NotFoundException(path);
            }
        }

        return current;
    }
}