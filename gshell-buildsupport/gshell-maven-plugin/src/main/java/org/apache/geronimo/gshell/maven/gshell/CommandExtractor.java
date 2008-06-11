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

package org.apache.geronimo.gshell.maven.gshell;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.geronimo.gshell.model.command.CommandModel;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts {@link org.apache.geronimo.gshell.model.command.CommandModel} instances from class files.
 *
 * @version $Id$
 */
public class CommandExtractor
{
    public static enum Scope
    {
        COMPILE,
        TEST; // ';' Here to keep QDox from being a bitch
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    private CommandGleaner gleaner = new CommandGleaner();

    public List<CommandModel> extract(final MavenProject project, final Scope scope) throws Exception {
        assert project != null;
        assert scope != null;

        log.debug("Gleaner: {}, scope: {}", gleaner, scope);

        List classpath;
        File classesDir;

        if (scope == Scope.COMPILE) {
            classpath = project.getCompileClasspathElements();
            classesDir = new File(project.getBuild().getOutputDirectory());
        }
        else if (scope == Scope.TEST) {
            classpath = project.getTestClasspathElements();
            classesDir = new File(project.getBuild().getTestOutputDirectory());
        }
        else {
            throw new InternalError();
        }

        if (!classesDir.exists()) {
            log.warn("Skipping descriptor generation; missing classes directory: {}", classesDir);

            return Collections.emptyList();
        }

        final ClassLoader prev = Thread.currentThread().getContextClassLoader();
        final ClassLoader cl = createClassLoader(classpath);

        Thread.currentThread().setContextClassLoader(cl);

        try {
            return extract(classesDir, cl);
        }
        finally {
            Thread.currentThread().setContextClassLoader(prev);
        }
    }

    private ClassLoader createClassLoader(final List elements) throws Exception {
        List<URL> list = new ArrayList<URL>();

        // Add the projects dependencies
        for (Object element : elements) {
            String filename = (String) element;

            try {
                list.add(new File(filename).toURI().toURL());
            }
            catch (MalformedURLException e) {
                throw new MojoExecutionException("Invalid classpath entry: " + filename, e);
            }
        }

        URL[] urls = list.toArray(new URL[list.size()]);

        log.debug("Classpath:");
        
        for (URL url : urls) {
            log.debug("    " + url);
        }

        return new URLClassLoader(urls, getClass().getClassLoader());
    }

    private List<CommandModel> extract(final File classesDir, final ClassLoader cl) throws Exception {
        assert classesDir != null;
        assert cl != null;

        List<CommandModel> descriptors = new ArrayList<CommandModel>();

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(classesDir);
        scanner.addDefaultExcludes();
        scanner.setIncludes(new String[]{ "**/*.class" });

        log.debug("Scanning for classes in: {}", classesDir);

        scanner.scan();

        for (String include : scanner.getIncludedFiles()) {
            String className = include.substring(0, include.lastIndexOf(".class")).replace('\\', '.').replace('/', '.');

            log.debug("Loading class: {}", className);

            try {
                Class type = cl.loadClass(className);

                log.debug("Gleaning from: {}", type);

                CommandModel model = gleaner.glean(type);
                
                if (model != null) {
                    descriptors.add(model);
                }
            }
            catch (VerifyError e) {
                log.error("Failed to load class: " + className + "; cause: " + e);
            }
        }

        log.debug("Extracted {} descriptor(s)", descriptors.size());

        return descriptors;
    }
}