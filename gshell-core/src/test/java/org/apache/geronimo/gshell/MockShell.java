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

package org.apache.geronimo.gshell;

import junit.framework.TestCase;
import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.command.CommandNotFoundException;
import org.apache.geronimo.gshell.command.CommandException;

/**
 * Mock {@link Shell}.
 *
 * @version $Id$
 */
public class MockShell
    extends Shell
{
    public String[] args;

    public String commandName;

    public MockShell() throws CommandException {
        super(new IO());
    }

    public int execute(String... args) throws Exception {
        this.args = args;

        return 0;
    }

    public int execute(String commandName, String[] args) throws Exception {
        this.commandName = commandName;
        this.args = args;

        return 0;
    }
}
