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

import java.util.Set;
import java.util.Collection;

/**
 * Interface for manager of command definitions and provides access to command instances.
 *
 * @version $Id$
 */
public interface CommandManager
{
    //
    // TODO: Rename, this has become a registry
    //

    boolean addCommandDefinition(CommandDefinition def);

    CommandDefinition getCommandDefinition(String name) throws CommandNotFoundException;

    Collection<CommandDefinition> commandDefinitions();

    Set<String> commandNames();
}
