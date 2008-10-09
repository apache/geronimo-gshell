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

package org.apache.geronimo.gshell.registry;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandException;
import org.apache.geronimo.gshell.command.Variables;

import java.util.Collection;

/**
 * Resolves {@link Command} instances for a given path.
 *
 * @version $Rev$ $Date$
 */
public interface CommandResolver
{
    String PATH = "gshell.path";

    String PATH_SEPARATOR = ":";

    String GROUP = "gshell.group";

    String COMMANDS_ROOT = "meta:/commands";

    String ALIASES_ROOT = "meta:/aliases";

    Command resolveCommand(String name, Variables variables) throws CommandException;

    Collection<Command> resolveCommands(String name, Variables variables) throws CommandException;
}