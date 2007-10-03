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

/**
 * Provides the basic interface for commands.
 *
 * @version $Rev$ $Date$
 */
public interface Command
{
    /** Standard command success status code. */
    Result SUCCESS = Result.SUCCESS;

    /** Standard command failure status code. */
    Result FAILURE = Result.FAILURE;

    /**
     * Command id
     *
     * @return
     */
    String getId();

    /**
     * Description of the command
     * @return
     */
    String getDescription();

    /**
     * Execute the commands behavior.
     */
    Object execute(CommandContext context, Object... args) throws Exception;

    //
    // Result
    //

    enum Result
    {
        SUCCESS,
        FAILURE
    }
}
