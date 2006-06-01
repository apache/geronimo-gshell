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

import org.apache.geronimo.gshell.console.InteractiveConsole;
import org.apache.geronimo.gshell.console.JLineConsole;
import org.apache.geronimo.gshell.console.IO;

import java.io.IOException;

/**
 * Provides the user-interaction bits for GShell.
 *
 * @version $Id$
 */
public class InteractiveGShell
    extends InteractiveConsole
{
    public InteractiveGShell(final IO io, final GShell gshell) throws IOException {
        super(new JLineConsole(io),

            new InteractiveConsole.Executor() {
                public Result execute(final String line) throws Exception {
                    assert line != null;

                    // Execute unless the line is just blank
                    if (!line.trim().equals("")) {
                        try {
                            gshell.execute(line);
                        }
                        catch (ExitNotification n) {
                            return Result.STOP;
                        }
                    }

                    return Result.CONTINUE;
                }
            },
                
            new InteractiveConsole.Prompter() {
                public String getPrompt() {
                    //
                    // TODO: Need to hook this up to allow it to change
                    //

                    return "> ";
                }
            });
    }
}
