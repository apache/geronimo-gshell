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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.geronimo.gshell.branding.Branding;
import org.apache.geronimo.gshell.command.CommandExecutor;
import org.apache.geronimo.gshell.remote.RemoteShell;
import org.apache.geronimo.gshell.shell.Environment;
import org.apache.geronimo.gshell.shell.ShellInfo;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.InstantiationStrategy;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the server-side encapsulation of the basic shell bits to allow remote clients to invoke commands.
 *
 * @version $Rev$ $Date$
 */
@Component(role=RemoteShell.class, instantiationStrategy=InstantiationStrategy.PER_LOOKUP)
public class DefaultRemoteShell
    implements RemoteShell, Initializable
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private ShellInfo shellInfo;

    @Requirement
    private Branding branding;

    @Requirement
    private CommandExecutor executor;

    @Requirement
    private Environment env;

    private boolean opened = true;
    
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

    public void initialize() throws InitializationException {
        /*

        FIXME: Client needs to request this to be done... ?

        try {
            loadProfileScripts();
        }
        catch (Exception e) {
            throw new InitializationException(e.getMessage(), e);
        }
        */
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
    // Script Processing
    //

    private void loadProfileScripts() throws Exception {
        //
        // TODO: Load gsh.properties if it exists?
        //

        // Load profile scripts if they exist
        loadSharedScript(branding.getProfileScriptName());
        loadUserScript(branding.getProfileScriptName());
    }

    private void loadScript(final File file) throws Exception {
        assert file != null;

        //
        // FIXME: Don't use 'source 'for right now, the shell spins out of control from plexus component loading :-(
        //
        // execute("source", file.toURI().toURL());

        BufferedReader reader = new BufferedReader(new FileReader(file));

        try {
            String line;

            while ((line = reader.readLine()) != null) {
                execute(line);
            }
        }
        finally {
            IOUtil.close(reader);
        }
    }

    private void loadUserScript(final String fileName) throws Exception {
        assert fileName != null;

        File file = new File(branding.getUserDirectory(), fileName);

        if (file.exists()) {
            log.debug("Loading user-script: {}", file);

            loadScript(file);
        }
        else {
            log.debug("User script is not present: {}", file);
        }
    }

    private void loadSharedScript(final String fileName) throws Exception {
        assert fileName != null;

        File file = new File(branding.getSharedDirectory(), fileName);

        if (file.exists()) {
            log.debug("Loading shared-script: {}", file);

            loadScript(file);
        }
        else {
            log.debug("Shared script is not present: {}", file);
        }
    }
}