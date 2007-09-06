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
import org.apache.bsf.BSFManager;
import org.apache.geronimo.gshell.JLineConsole;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.console.Console;

/**
 * Provides generic scripting language integration via <a href="http://http://jakarta.apache.org/bsf">BSF</a>.
 *
 * @version $Rev$ $Date$
 */
@CommandComponent(id="script")
public class ScriptCommand
    extends CommandSupport
{
    private String language;

    @Option(name="-l", aliases={"--language"}, required=true, description="Specify the scripting language")
    private void setLanguage(final String language) {
        assert language != null;
        
        if (!BSFManager.isLanguageRegistered(language)) {
            throw new RuntimeException("Language is not registered: " + language);
        }

        this.language = language;
    }

    @Option(name="-i", aliases={"--interactive"}, description="Run interactive mode")
    private boolean interactive;

    @Option(name="-e", aliases={"--expression"}, description="Evaluate the given expression")
    private String expression;

    protected Object doExecute() throws Exception {
        //
        // TODO: When given a file/url, try to figure out language from ext if language not given
        //

        BSFManager manager = new BSFManager();
        final BSFEngine engine = manager.loadScriptingEngine(language);

        if (this.expression != null) {
            log.info("Evaluating expression: " + expression);

            Object obj = engine.eval("<unknown>", 1, 1, expression);

            log.info("Expression result: " + obj);
        }
        else {
            // No expression, assume interactive (else we don't do anything)
            interactive = true;

            //
            // TODO: This will change when file/URL processing is added
            //
        }

        if (this.interactive) {
            log.debug("Starting interactive console...");
            
            Console.Executor executor = new Console.Executor() {
                public Result execute(final String line) throws Exception {
                    // Execute unless the line is just blank
                    
                    if (!line.trim().equals("")) {
                        engine.exec("<unknown>", 1, 1, line);
                    }

                    return Result.CONTINUE;
                }
            };

            JLineConsole runner = new JLineConsole(executor, io);

            runner.setErrorHandler(new Console.ErrorHandler() {
                public Result handleError(final Throwable error) {
                    log.error("Script evalutation failed: " + error, error);

                    return Result.CONTINUE;
                }
            });

            runner.setPrompter(new Console.Prompter() {
                public String prompt() {
                    return language + "> ";
                }
            });

            runner.run();
        }

        return SUCCESS;
    }
}
