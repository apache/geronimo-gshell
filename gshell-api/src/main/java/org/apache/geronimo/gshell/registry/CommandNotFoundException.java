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

import org.apache.geronimo.gshell.command.CommandException;

/**
 * Thrown to indicate a command/path was not able to be resolved.
 *
 * @version $Rev$ $Date$
 */
public class CommandNotFoundException
    extends CommandException
{
    public CommandNotFoundException(final String msg) {
        super(msg);
    }

    public CommandNotFoundException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public CommandNotFoundException(final Throwable cause) {
        super(cause);
    }

    public CommandNotFoundException() {
        super();
    }
}