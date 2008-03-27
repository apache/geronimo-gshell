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

import java.io.File;
import java.net.MalformedURLException;

import org.apache.geronimo.gshell.GShell;
import org.apache.geronimo.gshell.support.plexus.GShellPlexusContainer;
import org.apache.geronimo.gshell.plugin.CommandDiscoverer;
import org.apache.geronimo.gshell.plugin.CommandDiscoveryListener;
import org.apache.geronimo.gshell.command.IO;
import org.apache.geronimo.gshell.model.application.Application;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private Application application;

    private GShellPlexusContainer container;

    public GShellLoader(final ClassWorld classWorld, final IO io) throws Exception {
        assert classWorld != null;
        assert io != null;

        this.classWorld = classWorld;
        this.io = io;
    }

    public GShellLoader(final IO io) throws Exception {
        this(new ClassWorld("gshell", Thread.currentThread().getContextClassLoader()), io);
    }

    public GShellLoader() throws Exception {
        this(new IO());
    }

    public ArtifactRepository createLocalRepository(final File dir) throws ComponentLookupException, MalformedURLException {
        assert dir != null;
        
        ArtifactRepositoryLayout layout = container.lookupComponent(ArtifactRepositoryLayout.class, "default");

        DefaultArtifactRepository repo = new DefaultArtifactRepository("local",
            dir.toURI().toURL().toExternalForm(),
            layout,
            new ArtifactRepositoryPolicy(),  // snapshots
            new ArtifactRepositoryPolicy()); // releases

        repo.setBasedir(dir.getAbsolutePath());

        return repo;
    }

    public GShell load() throws Exception {
        return null;
    }

    public void test() throws Exception {
        ContainerConfiguration config = new DefaultContainerConfiguration();
        config.setName("gshell");
        config.setClassWorld(classWorld);
        // config.addComponentDiscoverer(new CommandDiscoverer());
        // config.addComponentDiscoveryListener(new CommandDiscoveryListener());

        container = new GShellPlexusContainer(config);

        File repositoryDir = new File(new File(System.getProperty("user.home")), ".gshell/repository");
        ArtifactRepository repository = createLocalRepository(repositoryDir);

        System.out.println("Repository: " + repository);
    }

    public static void main(final String[] args) throws Exception {
        GShellLoader loader = new GShellLoader();
        loader.test();
    }
}