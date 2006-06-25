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
import org.apache.geronimo.gshell.console.Console;
import org.apache.geronimo.gshell.console.JLineConsole;
import org.apache.geronimo.gshell.command.StandardVariables;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

import jline.ConsoleReader;
import jline.SimpleCompletor;

/**
 * Provides the user-interaction bits for Shell.
 *
 * @version $Id$
 */
public class InteractiveShell
    extends InteractiveConsole
{
    private static final Log log = LogFactory.getLog(InteractiveConsole.class);

    //
    // TODO: Refactor InteractiveConsole to use method overides for extention
    //       Seems like that is what we are doing so far anyways (sub-classing that is)
    //

    public InteractiveShell(final Console console, final Shell shell) throws IOException {
        super(console,
            new InteractiveConsole.Executor() {
                public Result execute(final String line) throws Exception {
                    assert line != null;

                    // Execute unless the line is just blank
                    if (line.trim().length() != 0) {
                        try {
                            shell.execute(line);
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
                    Object prompt = shell.getVariables().get(StandardVariables.PROMPT);
                    if (prompt == null) {
                        prompt = "";
                    }

                    return String.valueOf(prompt);
                }
            });

        // Add a command completer of we are using JLine
        if (console instanceof JLineConsole) {
            ConsoleReader jline = ((JLineConsole)console).getReader();
            jline.setCompletionHandler(new CompletionHandlerImpl());

            //
            // TODO: Need to include aliases too.  Probably want to add a custom completor
            //       to complete command options, etc.
            //
            String[] commands = (String[])shell.getCommandManager().commandNames().toArray(new String[0]);

            SimpleCompletor c = new SimpleCompletor(commands);
            jline.addCompletor(c);
        }
    }
}
