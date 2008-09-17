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

import org.apache.geronimo.gshell.shell.ShellInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Provides some runtime information about the shell.
 *
 * @version $Rev$ $Date$
 */
public class ShellInfoImpl
    implements ShellInfo
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private InetAddress localHost;

    private File homeDir;

    public ShellInfoImpl() {}

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

    @PostConstruct
    private void init() {
        homeDir = detectHomeDir();

        log.debug("Using home directory: {}", homeDir);

        try {
            localHost = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e) {
            throw new RuntimeException("Unable to determine locahost", e);
        }
    }

    private File detectHomeDir() {
        String homePath = System.getProperty("user.home");

        // And now lets resolve this sucker
        File dir;

        try {
            dir = new File(homePath).getCanonicalFile();
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to resolve home directory: " + homePath, e);
        }

        // And some basic sanity too
        if (!dir.exists() || !dir.isDirectory()) {
            throw new RuntimeException("Home directory configured but is not a valid directory: " + dir);
        }

        return dir;
    }
}