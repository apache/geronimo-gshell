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

package org.apache.geronimo.gshell.remote.server;

import org.apache.geronimo.gshell.command.CommandExecutor;
import org.apache.geronimo.gshell.remote.RemoteShell;
import org.apache.geronimo.gshell.shell.Environment;
import org.apache.geronimo.gshell.shell.ShellInfo;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the server-side encapsulation of the basic shell bits to allow remote clients to invoke commands.
 *
 * @version $Rev$ $Date$
 */
@Component(role=RemoteShell.class, instantiationStrategy="per-lookup")
public class DefaultRemoteShell
    implements RemoteShell
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private ShellInfo shellInfo;

    @Requirement
    private CommandExecutor executor;

    @Requirement
    private Environment env;

    private boolean opened = true;

    public DefaultRemoteShell() {
    }

    public DefaultRemoteShell(final ShellInfo shellInfo, final CommandExecutor executor, final Environment env) {
        this.shellInfo = shellInfo;
        this.executor = executor;
        this.env = env;
    }
    
    private void ensureOpened() {
        if (!opened) {
            throw new IllegalStateException("Not opened");
        }
    }

    public boolean isOpened() {
        return opened;
    }

    public void close() {
        log.debug("Closing");
        
        opened = false;
    }

    public Environment getEnvironment() {
        ensureOpened();
        
        return env;
    }

    public ShellInfo getShellInfo() {
        ensureOpened();

        return shellInfo;
    }

    //
    // Command Execution (all delegates)
    //

    public Object execute(final String line) throws Exception {
        ensureOpened();

        return executor.execute(line);
    }

    public Object execute(final Object... args) throws Exception {
        ensureOpened();

        return executor.execute((Object[])args);
    }

    public Object execute(final String path, final Object[] args) throws Exception {
        ensureOpened();

        return executor.execute(path, args);
    }

    //
    // TODO: Hookup profile script processing bits
    //
}