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
import java.io.IOException;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Bootstrap configuration.
 *
 * @version $Rev$ $Date$
 */
public class Configuration
{
    private static final String GSHELL_HOME = "gshell.home";

    private static final String PROGRAM_NAME = "program.name";

    private static final String DEFAULT_PROGRAM_NAME = "gsh";

    private static final String LOG4J_CONF = "log4j.configuration";

    private static final String DEFAULT_LOG4J_CONF = "org/apache/geronimo/gshell/bootstrap/default-log4j.xml";

    private File homeDir;

    private String programName;

    private String log4jConfig;

    private ClassLoader classLoader;

    public File getHomeDir() throws IOException {
        if (homeDir == null) {
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

            homeDir = dir;
        }

        return homeDir;
    }

    public String getProgramName() {
        if (programName == null) {
            programName = System.getProperty(PROGRAM_NAME, DEFAULT_PROGRAM_NAME);
        }

        return programName;
    }

    public File getProgramConfigurationFile(final String filename) throws IOException {
        assert filename != null;

        File etcDir = new File(getHomeDir(), "etc");
        File file = new File(etcDir, getProgramName() + "-" + filename);

        if (!file.exists()) {
            File unprefixedFile = new File(etcDir, filename);

            if (unprefixedFile.exists()) {
                file = unprefixedFile;
            }
        }

        return file;
    }

    public String getLog4jConfig() throws IOException {
        if (log4jConfig == null) {
            String path = System.getProperty(LOG4J_CONF);
            File file;

            if (path == null) {
                file = getProgramConfigurationFile("log4j.xml");
            }
            else {
                file = new File(path).getCanonicalFile();
            }

            if (file.exists()) {
                log4jConfig = file.toURI().toURL().toExternalForm();
            }
            else {
                log4jConfig = DEFAULT_LOG4J_CONF;
            }
        }

        return log4jConfig;
    }

    private void setProperty(final String name, final String value) {
        System.setProperty(name, value);

        if (Log.DEBUG) {
            Log.debug("Property: " + name + "=" + value);
        }
    }

    public void configure() throws Exception {
        setProperty(PROGRAM_NAME, getProgramName());
        setProperty(GSHELL_HOME, getHomeDir().getAbsolutePath());
        setProperty(LOG4J_CONF, getLog4jConfig());
    }

    public ClassLoader getClassLoader() throws Exception {
        if (classLoader == null) {
            List<URL> classPath = new ArrayList<URL>();

            // Add ${gshell.home}/etc
            classPath.add(new File(getHomeDir(), "etc").toURI().toURL());

            // Add ${gshell.home}/lib/*.jar
            File libDir = new File(getHomeDir(), "lib");
            File[] files = libDir.listFiles(new FileFilter() {
                public boolean accept(final File file) {
                    return file.isFile();
                }
            });

            if (files == null) {
                throw new Error("No jars found under: " + libDir);
            }
            
            for (File file : files) {
                classPath.add(file.toURI().toURL());
            }

            if (Log.DEBUG) {
                Log.debug("Classpath:");
                for (URL url : classPath) {
                    Log.debug("    " + url);
                }
            }

            classLoader = new URLClassLoader(classPath.toArray(new URL[classPath.size()]), getClass().getClassLoader());
        }

        return classLoader;
    }
}