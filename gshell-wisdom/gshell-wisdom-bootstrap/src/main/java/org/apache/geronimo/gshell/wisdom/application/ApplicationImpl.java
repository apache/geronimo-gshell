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

package org.apache.geronimo.gshell.wisdom.application;

import org.apache.geronimo.gshell.application.Application;
import org.apache.geronimo.gshell.application.ApplicationConfiguration;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.model.application.ApplicationModel;
import org.apache.geronimo.gshell.model.common.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Set;
import java.util.LinkedHashSet;

/**
 * Event fired once the application has constructed a shell.
 *
 * @version $Rev$ $Date$
 */
public class ApplicationImpl
    implements Application
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ApplicationConfiguration config;

    private Set<Artifact> artifacts;

    private InetAddress localHost;

    private File homeDir;

    public ApplicationImpl(final ApplicationConfiguration config) throws Exception {
        assert config != null;

        this.config = config;
        this.homeDir = detectHomeDir();
        this.localHost = InetAddress.getLocalHost();
    }

    public String getId() {
        return config.getModel().getId();
    }

    public IO getIo() {
        return config.getIo();
    }

    public Variables getVariables() {
        return config.getVariables();
    }

    public Set<Artifact> getArtifacts() {
        if (artifacts == null) {
            throw new IllegalStateException("Artifacts not initialized");
        }
        return artifacts;
    }

    void initArtifacts(final Set<org.apache.maven.artifact.Artifact> artifacts) {
        assert artifacts != null;

        Set<Artifact> set = new LinkedHashSet<Artifact>();

        log.debug("Application artifacts:");

        for (org.apache.maven.artifact.Artifact source : artifacts) {
            Artifact artifact = new Artifact();
            artifact.setGroupId(source.getGroupId());
            artifact.setArtifactId(source.getArtifactId());
            artifact.setType(source.getType());
            artifact.setVersion(source.getVersion());
            
            log.debug("    {}", artifact.getId());

            set.add(artifact);
        }

        this.artifacts = set;
    }

    public ApplicationModel getModel() {
        return config.getModel();
    }

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