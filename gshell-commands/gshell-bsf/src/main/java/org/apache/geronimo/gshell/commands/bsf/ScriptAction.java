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
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileUtil;
import org.apache.geronimo.gshell.ansi.Code;
import org.apache.geronimo.gshell.ansi.Renderer;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.console.Console;
import org.apache.geronimo.gshell.console.JLineConsole;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.spring.BeanContainerAware;
import org.apache.geronimo.gshell.vfs.FileSystemAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides generic scripting language integration via <a href="http://http://jakarta.apache.org/bsf">BSF</a>.
 *
 * @version $Rev$ $Date$
 */
public class ScriptAction
    implements CommandAction, BeanContainerAware
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final BSFManager manager;

    private final FileSystemAccess fileSystemAccess;

    private BeanContainer container;

    private String language;

    @Option(name="-l", aliases={"--language"})
    private void setLanguage(final String language) {
        assert language != null;
        
        if (!BSFManager.isLanguageRegistered(language)) {
            throw new RuntimeException("Language is not registered: " + language);
        }

        this.language = language;
    }

    @Option(name="-e", aliases={"--expression"})
    private String expression;

    @Argument
    private String path;

    public ScriptAction(final BSFManager manager, final FileSystemAccess fileSystemAccess) {
        assert manager != null;
        this.manager = manager;
        assert fileSystemAccess != null;
        this.fileSystemAccess = fileSystemAccess;
    }

    public void setBeanContainer(final BeanContainer container) {
        assert container != null;

        this.container = container;
    }

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        if (expression != null && path != null) {
            io.error("Can only specify an expression or a script file");
            return Result.FAILURE;
        }
        else if (expression != null) {
            return eval(context);
        }
        else if (path != null){
        	return exec(context);
        }

        return console(context);
    }

    private String detectLanguage(final FileObject file) throws Exception {
        assert file != null;

        return BSFManager.getLangFromFilename(file.getName().getBaseName());
    }

    private BSFEngine createEngine(final CommandContext context) throws BSFException {
        assert context != null;

        // Bind some stuff into the scripting engine's namespace
        manager.declareBean("container", container, BeanContainer.class);
        manager.declareBean("context", context, CommandContext.class);

        BSFEngine engine = manager.loadScriptingEngine(language);

        log.debug("Created engine: {}", engine);

        return engine;
    }

    private Object eval(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        if (language == null) {
            io.error("The scripting language must be configured via --language to evaluate an expression");
            return Result.FAILURE;
        }

        log.debug("Evaluating script ({}): {}", language, expression);

        BSFEngine engine = createEngine(context);

        try {
            return engine.eval("<script.expression>", 1, 1, expression);
        }
        finally {
            engine.terminate();
        }
    }

    private Object exec(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        FileObject cwd = fileSystemAccess.getCurrentDirectory(context.getVariables());
        FileObject file = fileSystemAccess.resolveFile(cwd, path);

        if (!file.exists()) {
            io.error("File not found: {}", file.getName());
            return Result.FAILURE;
        }
        else if (!file.getType().hasContent()) {
            io.error("File has not content: {}", file.getName());
            return Result.FAILURE;
        }
        else if (!file.isReadable()) {
            io.error("File is not readable: {}", file.getName());
            return Result.FAILURE;
        }

        if (language == null) {
            language = detectLanguage(file);
        }

        BSFEngine engine = createEngine(context);

        byte[] bytes = FileUtil.getContent(file);
        String script = new String(bytes);

        log.info("Evaluating file ({}): {}", language, path);

        try {
            return engine.eval(file.getName().getBaseName(), 1, 1, script);
        }
        finally {
            engine.terminate();
            file.close();
        }
    }

    private Object console(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        if (language == null) {
            io.error("The scripting language must be configured via --language to run an interactive console");
            return Result.FAILURE;
        }

        log.debug("Starting console ({})...", language);

        final BSFEngine engine = createEngine(context);
        final ResultHolder holder = new ResultHolder();

        Console.Executor executor = new Console.Executor() {
            public Result execute(final String line) throws Exception {
                if (line == null || line.trim().equals("exit") || line.trim().equals("quit")) {
                    return Result.STOP;
                }
                else if (!line.trim().equals("")) {
                    holder.result = engine.eval("<script.console>", 1, 1, line);
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
            Renderer renderer = new Renderer();

            public String prompt() {
                return renderer.render(Renderer.encode(language, Code.BOLD) + "> ");
            }
        });

        runner.run();

        engine.terminate();

        return holder.result;
    }

    private class ResultHolder
    {
        public Object result;
    }
}
