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

package org.apache.geronimo.gshell;

import org.apache.geronimo.gshell.command.CommandException;
import org.apache.geronimo.gshell.IO;

/**
 * Mock {@link Shell}.
 *
 * @version $Rev$ $Date$
 */
public class MockShell
    extends Shell
{
    public Object[] args;

    public String commandName;

    public MockShell() throws CommandException {
        super(new IO());
    }

    public Object execute(Object... args) throws Exception {
        this.args = args;

        return 0;
    }

    public Object execute(String commandName, Object[] args) throws Exception {
        this.commandName = commandName;
        this.args = args;

        return 0;
    }
}
