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

package org.apache.geronimo.gshell.commands.scripting;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.MessageSource;
import org.apache.geronimo.gshell.command.CommandException;
import org.apache.geronimo.gshell.console.JLineConsole;

import org.apache.bsf.BSFManager;
import org.apache.bsf.BSFEngine;

/**
 * Provides generic scripting language integration via <a href="http://http://jakarta.apache.org/bsf">BSF</a>.
 *
 * @version $Id$
 */
public class ScriptCommand
    extends CommandSupport
{
    private String language;

    private boolean interactive;
    
    private String expression;

    public ScriptCommand() {
        super("script");
    }

    protected Options getOptions() {
        MessageSource messages = getMessageSource();

        Options options = super.getOptions();

        options.addOption(OptionBuilder.withLongOpt("language")
            .withDescription(messages.getMessage("cli.option.language"))
            .hasArg()
            .create('l'));

        options.addOption(OptionBuilder.withLongOpt("expression")
            .withDescription(messages.getMessage("cli.option.expression"))
            .hasArg()
            .create('e'));

        options.addOption(OptionBuilder.withLongOpt("interactive")
            .withDescription(messages.getMessage("cli.option.interactive"))
            .create('i'));

        return options;
    }

    protected boolean processCommandLine(final CommandLine line) throws CommandException {
        assert line != null;

        if (line.hasOption('l')) {
            this.language = line.getOptionValue('l');
        }

        if (line.hasOption('e')) {
            this.expression = line.getOptionValue('e');
        }

        if (line.hasOption('i')) {
            this.interactive = true;
        }

        return false;
    }

    protected Object doExecute(final Object[] args) throws Exception {
        assert args != null;

        //
        // TODO: When given a file/url, try to figure out language from ext if language not given
        //

        if (language == null) {
            throw new RuntimeException("Must specify a language");
        }

        if (!BSFManager.isLanguageRegistered(language)) {
            throw new RuntimeException("Language is not registered: " + language);
        }

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
            InteractiveInterpreter interp = new InteractiveInterpreter(
                    new JLineConsole(getIO()), engine, language);
            interp.run();
        }

        return Command.SUCCESS;
    }
}
