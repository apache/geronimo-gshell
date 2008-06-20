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
 * Details about the runtime configuraiton of a command.
 *
 * @version $Rev$ $Date$
 */
public interface CommandInfo
{
    /**
     * Returns the <em>identifier</em> for the command.
     *
     * @return Command identifier.
     */
    String getId();

    /**
     * Returns the name of the command.
     *
     * @return Command name.
     */
    String getName();

    /**
     * Returns the alias used to invoke the command if any.
     *
     * @return The alias used to invoke the command; null if not aliased.
     */
    String getAlias();

    //
    // TODO: Add alias path?  And/or expose layout node?
    //

    /**
     * Returns the full path of the command.
     *
     * @return Command path.
     */
    String getPath();
}