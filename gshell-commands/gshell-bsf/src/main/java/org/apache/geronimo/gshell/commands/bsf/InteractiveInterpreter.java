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

package org.apache.geronimo.gshell.commands.bsf;

import org.apache.bsf.BSFEngine;
import org.apache.geronimo.gshell.console.Console;
import org.apache.geronimo.gshell.console.InteractiveConsole;

/**
 * An interactive console extention that knows how to execute lines of script text with a {@link BSFEngine}.
 *
 * @version $Rev$ $Date$
 */
public class InteractiveInterpreter
    extends InteractiveConsole
{
    public InteractiveInterpreter(final Console console, final BSFEngine engine, final String language) {
        super(console, new EngineExecutor(engine), new EnginePrompter(language));
    }

    private static class EngineExecutor
        implements Executor
    {
        private BSFEngine engine;

        public EngineExecutor(final BSFEngine engine) {
            this.engine = engine;
        }

        public Result execute(final String line) throws Exception {
            // Execute unless the line is just blank
            if (!line.trim().equals("")) {
                engine.exec("<unknown>", 1, 1, line);
            }

            return Result.CONTINUE;
        }
    }

    private static class EnginePrompter
        implements Prompter
    {
        private String language;

        public EnginePrompter(final String language) {
            this.language = language;
        }

        public String getPrompt() {
            return "script(" + language + ")> ";
        }
    }
}

