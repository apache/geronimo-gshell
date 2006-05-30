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

package org.apache.geronimo.gshell.console;

import junit.framework.TestCase;

/**
 * Unit tests for the {@link InteractiveConsole} class.
 *
 * @version $Id$
 */
public class InteractiveConsoleTest
    extends TestCase
{
    public void testConstructorArgs() throws Exception {
        try {
            new InteractiveConsole(null, null, null);
            fail("Accepted null value");
        }
        catch (IllegalArgumentException expected) {
            // ignore
        }

        try {
            new InteractiveConsole(new SimpleConsole(new IO()), null, null);
            fail("Accepted null value");
        }
        catch (IllegalArgumentException expected) {
            // ignore
        }

        // Happy day
        new InteractiveConsole(
            new SimpleConsole(new IO()),
            new InteractiveConsole.Executor() {
                public Result execute(String line) throws Exception {
                    return null;
                }
            },
            new InteractiveConsole.Prompter() {
                public String getPrompt() {
                    return null;
                }
            });
    }
}
