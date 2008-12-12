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
import java.io.FileFilter;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Bootstrap configuration implementation.
 *
 * @version $Rev$ $Date$
 */
public class ConfigurationImpl
    implements Configuration
{
    private static final String DEFAULT_PROPERTIES = "default.properties";
    
    private static final int SUCCESS_EXIT_CODE = 0;

    private static final int FAILURE_EXIT_CODE = 100;

    private static final String MAIN_CLASS = "org.apache.geronimo.gshell.cli.Main";

    private Properties props;

    private File homeDir;

    private File libDir;

    private File etcDir;

    private String programName;

    private void setSystemProperty(final String name, final String value) {
        assert name != null;
        assert value != null;

        if (Log.DEBUG) {
            Log.debug(name + ": " + value);
        }

        System.setProperty(name, value);
    }

    private Properties loadProperties() throws Exception {
        Properties props = new Properties();

        URL defaults = getClass().getResource(DEFAULT_PROPERTIES);
        assert defaults != null;

        if (Log.DEBUG) {
            Log.debug("Merging default properties from: " + defaults);
        }

        InputStream input = defaults.openStream();
        try {
            props.load(input);
        }
        finally {
            input.close();
        }

        // TODO: Resolve properties, merge in system?

        return props;
    }

    private String getProperty(final String name) {
        assert name != null;
        assert props != null;
        return props.getProperty(name);
    }

    public void configure() throws Exception {
        Log.debug("Configuring");
        
        this.props = loadProperties();

        // Export some configuration

        setSystemProperty(GSHELL_HOME, getHomeDir().getAbsolutePath());

        setSystemProperty(GSHELL_PROGRAM, getProgramName());
    }

    private void ensureConfigured() {
        if (props == null) {
            throw new IllegalStateException("Not configured");
        }
    }

    public File getHomeDir() {
        ensureConfigured();

        if (homeDir == null) {
            try {
                homeDir = detectHomeDir();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return homeDir;
    }

    /**
     * Attempt to detect the home directory, which is expected to be <tt>../../</tt> from the location of the jar containing this class.
     */
    private File detectHomeDir() throws Exception {
        if (Log.DEBUG) {
            Log.debug("Detecting " + GSHELL_HOME);
        }

        String path = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        path = URLDecoder.decode(path, "UTF-8");
        File file = new File(path);
        return file.getParentFile().getParentFile().getParentFile().getCanonicalFile();
    }

    public File getLibDir() {
        ensureConfigured();

        if (libDir == null) {
            // FIXME: Check for gshell.lib property
            libDir = new File(getHomeDir(), "lib");
        }

        return libDir;
    }

    public File getEtcDir() {
        ensureConfigured();

        if (etcDir == null) {
            // FIXME: Check for gshell.etc property
            etcDir = new File(getHomeDir(), "etc");
        }

        return etcDir;
    }

    public String getProgramName() {
        ensureConfigured();

        if (programName == null) {
            programName = getProperty(GSHELL_PROGRAM);
        }
        
        return programName;
    }

    public List<URL> getClassPath() throws Exception {
        ensureConfigured();
        List<URL> classPath = new ArrayList<URL>();

        classPath.add(getEtcDir().toURI().toURL());

        File[] files = getLibDir().listFiles(new FileFilter() {
            public boolean accept(final File file) {
                return file.isFile() && file.getName().toLowerCase().endsWith(".jar");
            }
        });

        if (files == null) {
            throw new Error("No jars found under: " + libDir);
        }

        for (File file : files) {
            classPath.add(file.toURI().toURL());
        }

        return classPath;
    }

    public String getMainClass() {
        return MAIN_CLASS;
    }

    public int getSuccessExitCode() {
        return SUCCESS_EXIT_CODE;
    }

    public int getFailureExitCode() {
        return FAILURE_EXIT_CODE;
    }
}