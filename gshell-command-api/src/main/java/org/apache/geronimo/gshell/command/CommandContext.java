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

import java.io.File;

import org.apache.geronimo.gshell.command.descriptor.CommandDescriptor;

/**
 * Provides commands with the context of its execution.
 *
 * @version $Rev: 572562 $ $Date: 2007-09-04 00:43:23 -0700 (Tue, 04 Sep 2007) $
 */
public interface CommandContext
{
    IO getIO();

    Variables getVariables();

    CommandDescriptor getCommandDescriptor();


    //
    // TODO: Split up the Shell's bits from the commands bits maybe?  Or expose a ShellInfo object for these bits maybe?
    //

    // File getHomeDir();
}