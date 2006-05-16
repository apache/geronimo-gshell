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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.command.VariablesMap;
import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.util.Arguments;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * ???
 *
 * @version $Id$
 */
public class GShellImpl
    implements ApplicationContextAware
{
    private static final Log log = LogFactory.getLog(GShell.class);

    private IO io;
    private ApplicationContext ctx;
    
    public void setIO(final IO io) {
        assert io != null;
        
        this.io = io;
    }
    
    public int execute(final String commandline) throws Exception {
        log.info("Executing (String): " + commandline);
        
        throw new Error("Not implemented, pending some parser work");
    }
    
    public int execute(final String commandName, String[] args) throws Exception {
        assert commandName != null;
        assert args != null;
        
        log.info("Executing (" + commandName + "): " + java.util.Arrays.asList(args));
        
        //
        // HACK: Just get something working right now
        //
        
        Command cmd = (Command)ctx.getBean(commandName);
        
        cmd.init(new CommandContext() {
            Variables vars = new VariablesMap();
            
            public IO getIO() {
                return io;
            }

            public Variables getVariables() {
                return vars;
            }
        });
        
        int status;
        
        try {
            status = cmd.execute(args);
        }
        finally {
            cmd.destroy();
        }
        
        return status;
    }
    
    public int execute(final String[] args) throws Exception {
        assert args != null;
        assert args.length > 1;
        
        log.info("Executing (String[]): " + java.util.Arrays.asList(args));
        
        return execute(args[0], Arguments.shift(args));
    }
    
    //
    // ApplicationContextAware
    //
    
    public void setApplicationContext(final ApplicationContext ctx) throws BeansException {
        assert ctx != null;
        
        this.ctx = ctx;
    }
}
