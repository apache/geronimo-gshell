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

/**
 * Thrown to indicate that an operation which requires a named
 * command has failed because the command was not found.
 *
 * @version $Revision$ $Date$
 */
public class CommandNotFoundException
   extends CommandException
{
    public CommandNotFoundException(final String path) {
        this(path, "Command or path was not found");
    }

    public CommandNotFoundException(final String path, final String msg) {
        super(msg + ": " + path);
    }
}