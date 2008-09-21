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

import org.apache.geronimo.gshell.commandline.CommandLineExecutor;
import org.apache.geronimo.gshell.remote.RemoteShell;
import org.apache.geronimo.gshell.shell.ShellContext;
import org.apache.geronimo.gshell.shell.ShellInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provides the server-side encapsulation of the basic shell bits to allow remote clients to invoke commands.
 *
 * @version $Rev$ $Date$
 */
public class RemoteShellImpl
    implements RemoteShell
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ShellInfo shellInfo;

    @Autowired
    private CommandLineExecutor executor;

    private boolean opened = true;

    public RemoteShellImpl() {}

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

    public ShellContext getContext() {
        ensureOpened();

        // FIXME:

        return null;
    }

    public ShellInfo getInfo() {
        ensureOpened();

        return shellInfo;
    }

    public Object execute(final String line) throws Exception {
        ensureOpened();

        // FIXME:

        return null;
    }

    public Object execute(final String command, final Object[] args) throws Exception {
        ensureOpened();

        // FIXME:

        return null;
    }

    public Object execute(final Object... args) throws Exception {
        ensureOpened();

        // FIXME:

        return null;
    }

    public Object execute(final Object[][] commands) throws Exception {
        ensureOpened();

        // FIXME:

        return null;
    }

    public boolean isInteractive() {
        return false;
    }

    public void run(final Object... args) throws Exception {
        throw new UnsupportedOperationException();
    }

    //
    // TODO: Hookup profile script processing bits
    //
}