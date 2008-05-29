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

package org.apache.geronimo.gshell;

import org.apache.geronimo.gshell.shell.ShellInfo;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Provides some runtime information about the shell.
 *
 * @version $Rev$ $Date$
 */
@Component(role=ShellInfo.class, hint="default")
public class DefaultShellInfo
    implements ShellInfo, Initializable
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private InetAddress localHost;

    private File homeDir;

    public DefaultShellInfo() {}
    
    public File getHomeDir() {
        if (homeDir == null) {
            throw new IllegalStateException();
        }

        return homeDir;
    }

    public InetAddress getLocalHost() {
        if (localHost == null) {
            throw new IllegalStateException();
        }

        return localHost;
    }

    public String getUserName() {
        return System.getProperty("user.name");
    }

    public void initialize() throws InitializationException {
        homeDir = detectHomeDir();
        
        log.debug("Using home directory: {}", homeDir);

        try {
            localHost = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e) {
            throw new InitializationException("Unable to determine locahost", e);
        }
    }
    
    private File detectHomeDir() throws InitializationException {
        //
        // FIXME:
        //

        String homePath = null; // branding.getProperty(Branding.HOME);

        if (homePath == null) {
            //
            // FIXME: For now just use the user's home ?  We may actually want to try and figure this our harder (like
            //        how Launcher does...
            //

            homePath = System.getProperty("user.home");
        }

        // And now lets resolve this sucker
        File dir;
        
        try {
            dir = new File(homePath).getCanonicalFile();
        }
        catch (IOException e) {
            throw new InitializationException("Failed to resolve home directory: " + homePath, e);
        }

        // And some basic sanity too
        if (!dir.exists() || !dir.isDirectory()) {
            throw new InitializationException("Home directory configured but is not a valid directory: " + dir);
        }

        return dir;
    }
}
