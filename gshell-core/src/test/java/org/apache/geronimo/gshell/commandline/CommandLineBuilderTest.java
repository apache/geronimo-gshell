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

package org.apache.geronimo.gshell.commandline;

import junit.framework.TestCase;
import org.apache.geronimo.gshell.MockShell;

/**
 * Unit tests for the {@link CommandLineBuilder} class.
 *
 * @version $Id$
 */
public class CommandLineBuilderTest
    extends TestCase
{
    public void testConstructor() throws Exception {
        try {
            new CommandLineBuilder(null);
            fail("Accepted null argument");
        }
        catch (IllegalArgumentException expected) {
            // ignore
        }
    }

    public void testSimple() throws Exception {
        MockShell shell = new MockShell();
        CommandLineBuilder builder = new CommandLineBuilder(shell);

        CommandLine cl = builder.create("echo hi");
        assertNotNull(cl);

        cl.execute();

        assertEquals("echo", shell.commandName);
        assertEquals(1, shell.args.length);
        assertEquals("hi", shell.args[0]);
    }
}
