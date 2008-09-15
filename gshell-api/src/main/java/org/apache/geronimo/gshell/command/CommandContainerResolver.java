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
 * Resolves {@link CommandContainer} instances for a given path.
 *
 * @version $Rev$ $Date$
 */
public interface CommandContainerResolver
{
    /**
     * Resolve the given path to a command container instance.
     *
     * @param variables     The current shell variables.
     * @param path          The path of the command to resolve.
     * @return              The resolved command container instance; never null.
     *
     * @throws CommandException     Failed to resolve command container.
     */
    CommandContainer resolve(Variables variables, String path) throws CommandException;
}