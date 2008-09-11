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

package org.apache.geronimo.gshell.wisdom.shell;

import org.apache.geronimo.gshell.application.ApplicationManager;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.commandline.CommandLine;
import org.apache.geronimo.gshell.commandline.CommandLineBuilder;
import org.apache.geronimo.gshell.commandline.CommandLineExecutor;
import org.apache.geronimo.gshell.notification.ErrorNotification;
import org.apache.geronimo.gshell.parser.ASTCommandLine;
import org.apache.geronimo.gshell.parser.CommandLineParser;
import org.apache.geronimo.gshell.parser.ParseException;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.spring.BeanContainerAware;
import org.apache.geronimo.gshell.wisdom.application.event.ApplicationConfiguredEvent;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.io.Reader;
import java.io.StringReader;

/**
 * Builds {@link CommandLine} instances ready for executing.
 *
 * @version $Rev$ $Date$
 */
public class CommandLineBuilderImpl
    implements CommandLineBuilder //, BeanContainerAware, ApplicationListener
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationManager applicationManager;

    @Autowired
    private CommandLineExecutor executor;

    // private BeanContainer container;

    private final CommandLineParser parser = new CommandLineParser();

    public CommandLineBuilderImpl() {}

    /*
    public void setBeanContainer(final BeanContainer container) {
        assert container != null;

        this.container = container;
    }

    //
    // TODO: See if we can @Autowire this puppy, since it looks like spring can handle the cirtcular reference?
    //
    
    public void onApplicationEvent(final ApplicationEvent event) {
        log.debug("Processing application event: {}", event);
        
        if (event instanceof ApplicationConfiguredEvent) {
            executor = container.getBean(CommandLineExecutor.class);
        }
    }
    */

    private ASTCommandLine parse(final String input) throws ParseException {
        assert input != null;

        Reader reader = new StringReader(input);
        ASTCommandLine cl;
        try {
            cl = parser.parse(reader);
        }
        finally {
            IOUtil.close(reader);
        }

        // If debug is enabled, the log the parse tree
        if (log.isDebugEnabled()) {
            LoggingVisitor logger = new LoggingVisitor(log);
            cl.jjtAccept(logger, null);
        }

        return cl;
    }

    public CommandLine create(final String commandLine) throws ParseException {
        assert commandLine != null;

        if (commandLine.trim().length() == 0) {
            throw new IllegalArgumentException("Command line is empty");
        }

        try {
            assert applicationManager != null;
            Variables vars = applicationManager.getContext().getVariables();

            assert executor != null;
            final ExecutingVisitor visitor = new ExecutingVisitor(executor, vars);
            final ASTCommandLine root = parse(commandLine);

            return new CommandLine() {
                public Object execute() throws Exception {
                    return root.jjtAccept(visitor, null);
                }
            };
        }
        catch (Exception e) {
            throw new ErrorNotification(e);
        }
    }
}