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

import org.apache.geronimo.gshell.command.IO;
import org.apache.geronimo.gshell.common.StopWatch;
import org.apache.geronimo.gshell.lookup.EnvironmentLookup;
import org.apache.geronimo.gshell.lookup.IOLookup;
import org.apache.geronimo.gshell.shell.Environment;
import org.apache.geronimo.gshell.shell.InteractiveShell;
import org.apache.geronimo.gshell.shell.ShellInfo;
import org.apache.geronimo.gshell.url.URLHandlerFactory;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple facade over GShell, sets up the container for you... you just run commands :-)
 *
 * @version $Rev$ $Date$
 */
public class GShell
    implements InteractiveShell
{
    private Logger log = LoggerFactory.getLogger(getClass());

    private final ClassWorld classWorld;

    private final IO io;
    
    private final PlexusContainer container;

    private final SecurityManager sm;

    private final InteractiveShell shell;

    public GShell(final IO io) throws Exception {
        this(new ClassWorld("gshell", Thread.currentThread().getContextClassLoader()), io);
    }

    public GShell(final ClassWorld classWorld, final IO io) throws Exception {
        assert classWorld != null;
        assert io != null;

        this.classWorld = classWorld;
        this.io = io;

        // Lets time how long init takes
        StopWatch watch = new StopWatch(true);

        log.debug("Initializing");

        sm = new ShellSecurityManager();

        SecurityManager psm = System.getSecurityManager();
        System.setSecurityManager(sm);

        try {
            ContainerConfiguration config = new DefaultContainerConfiguration();
            config.setName("gshell.core");
            config.setClassWorld(classWorld);
            container = new DefaultPlexusContainer(config);

            //
            // TODO: Disabled for now, re-hookup once rfile bits are ready
            //
            // Get our URL handler factory installed
            // container.lookup(URLHandlerFactory.class);

            // We first need to stuff in the IO context for the new shell instance
            IOLookup.set(container, io);

            // And then lets stuff in the environment too
            Environment env = new DefaultEnvironment(io);
            EnvironmentLookup.set(container, env);

            // Then look up the shell we are gonna delegate to
            shell = (InteractiveShell) container.lookup(InteractiveShell.class);
        }
        finally {
            System.setSecurityManager(psm);
        }

        log.debug("Initialized in {}", watch);
    }

    public ShellInfo getShellInfo() {
        return shell.getShellInfo();
    }

    public Environment getEnvironment() {
        return shell.getEnvironment();
    }

    public Object execute(final String line) throws Exception {
        SecurityManager psm = System.getSecurityManager();
        System.setSecurityManager(sm);

        try {
            return shell.execute(line);
        }
        finally {
            System.setSecurityManager(psm);
        }
    }

    public Object execute(final Object... args) throws Exception {
        SecurityManager psm = System.getSecurityManager();
        System.setSecurityManager(sm);

        try {
            return shell.execute((Object[])args);
        }
        finally {
            System.setSecurityManager(psm);
        }
    }

    public Object execute(final String path, final Object[] args) throws Exception {
        SecurityManager psm = System.getSecurityManager();
        System.setSecurityManager(sm);

        try {
            return shell.execute(path, args);
        }
        finally {
            System.setSecurityManager(psm);
        }
    }
    
    public void run(final Object... args) throws Exception {
        SecurityManager psm = System.getSecurityManager();
        System.setSecurityManager(sm);

        try {
            shell.run(args);
        }
        finally {
            System.setSecurityManager(psm);
        }
    }
}