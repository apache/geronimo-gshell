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

package org.apache.geronimo.gshell.loader;

import org.apache.geronimo.gshell.model.application.Application;
import org.apache.geronimo.gshell.model.settings.Settings;
import org.apache.geronimo.gshell.command.IO;
import org.apache.geronimo.gshell.lookup.IOLookup;
import org.apache.geronimo.gshell.lookup.EnvironmentLookup;
import org.apache.geronimo.gshell.shell.Environment;
import org.apache.geronimo.gshell.shell.InteractiveShell;
import org.apache.geronimo.gshell.DefaultEnvironment;
import org.apache.geronimo.gshell.security.ShellSecurityManager;
import org.apache.geronimo.gshell.GShell;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * Loads {@link org.apache.geronimo.gshell.GShell} instances.
 *
 * @version $Rev$ $Date$
 */
public class GShellLoader
{
    private Logger log = LoggerFactory.getLogger(getClass());

    private final ClassWorld classWorld;

    private final IO io;

    private final Environment environment;

    private final SecurityManager securityManager;

    private final PlexusContainer container;

    private Application application;

    private Settings settings;

    public GShellLoader(final ClassWorld classWorld, final IO io) throws Exception {
        assert classWorld != null;
        assert io != null;

        this.classWorld = classWorld;
        this.io = io;
        this.environment = new DefaultEnvironment(io);
        this.securityManager = new ShellSecurityManager();

        this.container = createContainer();

        // Install IO and Environment lookups
        IOLookup.set(container, io);
        EnvironmentLookup.set(container, environment);
    }

    private PlexusContainer createContainer() throws Exception {
        ContainerConfiguration config = new DefaultContainerConfiguration();
        config.setName("gshell.core");
        config.setClassWorld(classWorld);

        return new DefaultPlexusContainer(config);
    }

    private ArtifactRepository createArtifactRepository() throws ComponentLookupException {
        ArtifactRepositoryLayout repositoryLayout =
                (ArtifactRepositoryLayout) container.lookup(ArtifactRepositoryLayout.ROLE, "default");

        ArtifactRepositoryFactory artifactRepositoryFactory =
                (ArtifactRepositoryFactory) container.lookup(ArtifactRepositoryFactory.ROLE);

        String url = null; //settings.getLocalRepository();

        if (!url.startsWith("file:")) {
            url = "file://" + url;
        }

        ArtifactRepository localRepository = new DefaultArtifactRepository("local", url, repositoryLayout);

        /*
        boolean snapshotPolicySet = false;

        if (commandLine.hasOption(CLIManager.OFFLINE)) {
            settings.setOffline(true);

            snapshotPolicySet = true;
        }

        if (!snapshotPolicySet && commandLine.hasOption(CLIManager.UPDATE_SNAPSHOTS)) {
            artifactRepositoryFactory.setGlobalUpdatePolicy(ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS);
        }

        if (commandLine.hasOption(CLIManager.CHECKSUM_FAILURE_POLICY)) {
            System.out.println("+ Enabling strict checksum verification on all artifact downloads.");

            artifactRepositoryFactory.setGlobalChecksumPolicy(ArtifactRepositoryPolicy.CHECKSUM_POLICY_FAIL);
        } else if (commandLine.hasOption(CLIManager.CHECKSUM_WARNING_POLICY)) {
            System.out.println("+ Disabling strict checksum verification on all artifact downloads.");

            artifactRepositoryFactory.setGlobalChecksumPolicy(ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN);
        }
        */

        return localRepository;
    }

    //
    // Loading
    //

    public GShell load() throws Exception {
        InteractiveShell shell = (InteractiveShell) container.lookup(InteractiveShell.class);

        return null;
    }
}