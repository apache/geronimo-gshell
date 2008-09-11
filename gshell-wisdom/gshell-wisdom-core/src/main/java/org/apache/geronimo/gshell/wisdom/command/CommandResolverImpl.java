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

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandContainer;
import org.apache.geronimo.gshell.command.CommandContainerFactory;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.CommandInfo;
import org.apache.geronimo.gshell.command.CommandNotFoundException;
import org.apache.geronimo.gshell.command.CommandResolver;
import org.apache.geronimo.gshell.command.CommandResult;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.layout.LayoutManager;
import org.apache.geronimo.gshell.layout.NotFoundException;
import org.apache.geronimo.gshell.model.layout.AliasNode;
import org.apache.geronimo.gshell.model.layout.CommandNode;
import org.apache.geronimo.gshell.model.layout.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The default {@link CommandResolver} component.
 *
 * @version $Rev$ $Date$
 */
public class CommandResolverImpl
    implements CommandResolver
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private LayoutManager layoutManager;

    @Autowired
    private CommandContainerFactory containerFactory;

    public CommandResolverImpl() {}

    public Command resolve(final Variables variables, final String path) throws CommandNotFoundException {
        assert variables != null;
        assert path != null;

        assert layoutManager != null;

        log.debug("Resolving command for path: {}", path);

        String searchPath = (String) variables.get(LayoutManager.COMMAND_PATH);
        final Node node;

        try {
            node = layoutManager.findNode(path, searchPath);
        }
        catch (NotFoundException e) {
            throw new CommandNotFoundException(e);
        }

        String id = findCommandId(node);

        assert containerFactory != null;

        final CommandContainer container;
        try {
            container = containerFactory.create(id);
        }
        catch (Exception e) {
            throw new CommandNotFoundException(e);
        }

        // Return a new command-instance with details about it layout configuration
        return new Command()
        {
            public CommandInfo getInfo() {
                return new CommandInfoImpl(node);
            }

            public CommandContainer getContainer() {
                return container;
            }

            public CommandResult execute(CommandContext context) {
                return container.execute(context);
            }
        };
    }

    private String findCommandId(final Node node) throws CommandNotFoundException {
        assert node != null;

        assert layoutManager != null;

        if (node instanceof AliasNode) {
            AliasNode aliasNode = (AliasNode) node;
            String targetPath = aliasNode.getCommand();

            Node target;
            try {
                target = layoutManager.findNode(layoutManager.getLayout(), targetPath);
            }
            catch (NotFoundException e) {
                throw new CommandNotFoundException(e);
            }

            return findCommandId(target);
        }
        else if (node instanceof CommandNode) {
            CommandNode commandNode = (CommandNode) node;

            return commandNode.getId();
        }

        throw new CommandNotFoundException("Unable to get command id for: " + node);
    }
}