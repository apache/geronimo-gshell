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

package org.apache.geronimo.gshell.command;

import org.apache.geronimo.gshell.i18n.MessageSource;
import org.apache.geronimo.gshell.io.IO;

/**
 * Provides facilites for a configured command instance.
 *
 * @version $Rev$ $Date$
 */
public interface CommandContainer
{
    /**
     * Returns the configured identifier of the command.
     *
     * @return  The command identifier; never null.
     */
    String getId();

    /**
     * Returns the action of the command.
     *
     * @return  The command action; never null.
     */
    CommandAction getAction();

    /**
     * Returns the documenter for the command.
     *
     * @return  The command documenter; never null.
     */
    CommandDocumenter getDocumenter();

    /**
     * Returns the completer for the command.
     *
     * @return  The command completer; never null.
     */
    CommandCompleter getCompleter();

    /**
     * Returns the message source for the command.
     *
     * @return  The command message source; never null.
     */
    MessageSource getMessages();

    /**
     * Executes the commands configured action.
     * 
     * @param args          The arguments to the command; must not be null.
     * @param io            The I/O context for the command; must not be null.
     * @param variables     The environment variables for the command; must not be null.
     * @return              The command result; never null
     */
    CommandResult execute(Object[] args, IO io, Variables variables);
}