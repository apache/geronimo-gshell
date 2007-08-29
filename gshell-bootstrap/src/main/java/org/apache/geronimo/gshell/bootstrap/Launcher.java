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

/**
 * Platform independent launcher to setup common configuration and delegate to
 * the Classworlds launcher.
 *
 * @version $Rev$ $Date$
 */
public class Launcher
{
    private static boolean debug = Boolean.getBoolean(Launcher.class.getName() + ".debug");

    private static String programName;
    
    private static File homeDir;

    public static void main(final String[] args) throws Exception {
        assert args != null;

        programName = getProgramName();
        setProperty("program.name", programName);

        homeDir = getHomeDir();
        setProperty("gshell.home", homeDir.getCanonicalPath());

        File classworldsConf = getClassworldsConf();
        setProperty("classworlds.conf", classworldsConf.getCanonicalPath());

        File log4jConf = getLog4jConf();
        setProperty("log4j.configuration", log4jConf.toURI().toURL().toString());

        // Delegate to the Classworlds launcher to finish booting
        org.codehaus.plexus.classworlds.launcher.Launcher.main(args);
    }

    private static void debug(final String message) {
        if (debug) {
            System.err.println("[DEBUG] " + message);
        }
    }

    private static void warn(final String message) {
        System.err.println("[WARNING] " + message);
    }

    private static void setProperty(final String name, final String value) {
        System.setProperty(name, value);
        debug(name + "=" + value);
    }

    private static String getProgramName() {
        String name = System.getProperty("program.name");
        if (name == null) {
            name = "gsh";
        }

        return name;
    }

    private static File getHomeDir() throws Exception {
        String path = System.getProperty("gshell.home");
        File dir;

        if (path == null) {
            String jarPath = Launcher.class.getProtectionDomain().getCodeSource().getLocation().getFile();
            jarPath = java.net.URLDecoder.decode(jarPath);

            // The jar containing this class is expected to be in <gshell.home>/lib/boot
            File bootJar = new File(jarPath);
            dir = bootJar.getParentFile().getParentFile().getParentFile().getCanonicalFile();
        }
        else {
            dir = new File(path).getCanonicalFile();
        }

        return dir;
    }

    private static File getClassworldsConf() throws Exception {
        String path = System.getProperty("classworlds.conf");
        File file;

        if (path == null) {
            file = new File(homeDir, "etc/" + programName + "-classworlds.conf");
        }
        else {
            file = new File(path).getCanonicalFile();
        }

        return file;
    }

    private static File getLog4jConf() throws Exception {
        String path = System.getProperty("log4j.configuration");
        File file;

        if (path == null) {
            file = new File(homeDir, "etc/" + programName + "-log4j.properties");
        }
        else {
            file = new File(path).getCanonicalFile();
        }

        return file;
    }
}
