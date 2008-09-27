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

package org.apache.geronimo.gshell.bootstrap;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

/**
 * Platform independent launcher to setup common configuration and launch the application.
 *
 * @version $Rev$ $Date$
 */
public class Launcher
    extends org.codehaus.plexus.classworlds.launcher.Launcher
{
    private static final String PROGRAM_NAME = "program.name";

    private static final String GSHELL_HOME = "gshell.home";

    private static final String LOG4J_CONF = "log4j.configuration";

    private static final String DEFAULT_LOG4J_CONF = "org/apache/geronimo/gshell/bootstrap/log4j.xml";

    private static final String DEFAULT_CLASSWORLDS_CONF = "org/apache/geronimo/gshell/bootstrap/classworlds.conf";

    private static boolean debug = Boolean.getBoolean(Launcher.class.getName() + ".debug");

    private static String programName;
    
    private static File homeDir;

    private static void debug(final String message) {
        if (debug) {
            System.err.println("[DEBUG] " + message);
        }
    }

    private static void warn(final String message) {
        System.err.println("[WARN] " + message);
    }

    private static void error(final String message) {
        System.err.println("[ERROR] " + message);
        throw new Error(message);
    }

    private static void setProperty(final String name, final String value) {
        System.setProperty(name, value);
        debug("Setting property: " + name + "=" + value);
    }

    private static String getProgramName() {
        String name = System.getProperty(PROGRAM_NAME);
        if (name == null) {
            name = "gsh";
        }

        return name;
    }

    private static File getHomeDir() throws Exception {
        String path = System.getProperty(GSHELL_HOME);
        File dir;

        if (path == null) {
            String jarPath = Launcher.class.getProtectionDomain().getCodeSource().getLocation().getFile();
            jarPath = java.net.URLDecoder.decode(jarPath, "UTF-8");

            // The jar containing this class is expected to be in <gshell.home>/lib/boot
            File bootJar = new File(jarPath);
            dir = bootJar.getParentFile().getParentFile().getParentFile().getCanonicalFile();
        }
        else {
            dir = new File(path).getCanonicalFile();
        }

        return dir;
    }

    private static File getProgramConfigurationFile(final String filename) {
        assert filename != null;

        File etcDir = new File(homeDir, "etc");

        File file = new File(etcDir, programName + "-" + filename);

        if (!file.exists()) {
            File unprefixedFile = new File(etcDir, filename);
            
            if (unprefixedFile.exists()) {
                file = unprefixedFile;
            }
        }

        return file;
    }

    private static String getClassworldsConf() throws Exception {
        String path = System.getProperty(CLASSWORLDS_CONF);
        File file;

        if (path == null) {
            file = getProgramConfigurationFile("classworlds.conf");
        }
        else {
            file = new File(path).getCanonicalFile();
        }

        if (file.exists()) {
            return file.getCanonicalPath();
        }

        return DEFAULT_CLASSWORLDS_CONF;
    }

    private static String getLog4jConf() throws Exception {
        String path = System.getProperty(LOG4J_CONF);
        File file;

        if (path == null) {
            file = getProgramConfigurationFile("log4j.xml");
        }
        else {
            file = new File(path).getCanonicalFile();
        }

        if (file.exists()) {
            return file.toURI().toURL().toExternalForm();
        }

        return DEFAULT_LOG4J_CONF;
    }

    private static void configure() throws Exception {
        // Branding information is not available here, so we must use the basic GShell properties to configure the bootstrap loader.

        programName = getProgramName();
        setProperty(PROGRAM_NAME, programName);

        homeDir = getHomeDir();
        setProperty(GSHELL_HOME, homeDir.getCanonicalPath());

        String classworldsConf = getClassworldsConf();
        if (classworldsConf != null) {
            setProperty(CLASSWORLDS_CONF, classworldsConf);
        }

        String log4jConf = getLog4jConf();
        if (log4jConf != null) {
            setProperty(LOG4J_CONF, log4jConf);
        }
        else {
            warn("Missing Log4j configuration! Logging may not properly initialize;  Please set the '" +
                    LOG4J_CONF + "' property to a valid configuration file or resource name.");
        }
    }

    public static int mainWithExitCode(final String[] args) throws Exception {
        assert args != null;

        Launcher launcher = new Launcher();

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        launcher.setSystemClassLoader(cl);

        String classworldsConf = System.getProperty(CLASSWORLDS_CONF);
        URL configUrl = null;

        if (classworldsConf != null) {
            File file = new File(classworldsConf);
            if (file.exists()) {
                configUrl = file.toURI().toURL();
            }
            else {
                configUrl = cl.getResource(classworldsConf);
            }
        }

        if (configUrl == null) {
            error("Failed to locate ClassWorlds configuration!  Please set the '" +
                    CLASSWORLDS_CONF + "' property to a valid configuration file or resource name.");
            // Unreachable statement, ^^^ throws Error w/message
            throw new Error();
        }

        debug("Resolved " + CLASSWORLDS_CONF + ": " + configUrl);

        launcher.configure(configUrl.openStream());

        try {
            launcher.launch(args);
        }
        catch (InvocationTargetException e) {
            throw new LaunchFailedError(e.getTargetException());
        }

        return launcher.getExitCode();
    }

    public static void main(final String[] args) {
        assert args != null;

        try {
            configure();

            int exitCode = mainWithExitCode(args);

            System.exit(exitCode);
        }
        catch (Throwable t) {
            t.printStackTrace(System.err);
            System.err.flush();
            System.exit(100);
        }
    }

    //
    // LaunchFailedError
    //
    
    private static class LaunchFailedError
        extends Error
    {
        public LaunchFailedError(final Throwable cause) {
            super(cause);
        }

        public LaunchFailedError(final String msg) {
            super(msg);
        }
    }
}
