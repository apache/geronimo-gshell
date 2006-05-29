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
import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.command.CommandExecutor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * ???
 *
 * @version $Id$
 */
public class GShell
    implements CommandExecutor
{
    private static final Log log = LogFactory.getLog(GShell.class);
    
    private ApplicationContext ctx;

    private GShellImpl impl;
    
    public GShell(final IO io) {
        assert io != null;
        
        this.ctx = new ClassPathXmlApplicationContext(new String[] {
            "classpath*:/gshell.xml",
            "classpath*:/META-INF/org.apache.geronimo.gshell/components.xml",
        });
        
        this.impl = (GShellImpl)ctx.getBean("gshell");
        this.impl.setIO(io);
    }
    
    public GShell() {
        this(new IO());
    }
    
    public int execute(final String args) throws Exception {
        return impl.execute(args);
    }
    
    public int execute(final String[] args) throws Exception {
        return impl.execute(args);
    }
    
    public int execute(final String commandName, String[] args) throws Exception {
        return impl.execute(commandName, args);
    }
}
