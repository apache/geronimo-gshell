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
import java.io.StringWriter;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import org.apache.geronimo.gshell.plugin.model.Command;
import org.apache.geronimo.gshell.plugin.model.Plugin;
import org.apache.geronimo.gshell.plugin.model.io.xpp3.PluginXpp3Writer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import org.apache.xbean.finder.ClassFinder;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Parameter;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.cdc.ComponentDescriptor;
import org.codehaus.plexus.component.repository.cdc.ComponentRequirement;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 * Generates a GShell XML plugin descriptor.
 *
 * @goal plugin-descriptor
 * @phase process-classes
 * @requiresDependencyResolution runtime
 *
 * @version $Rev$ $Date$
 */
public class PluginDescriptorMojo
    extends AbstractMojo
{
    /**
     * The directory where class files have been built.
     *
     * @parameter expression="${project.build.outputDirectory}
     */
    private File classesDirectory;

    /**
     * The full list of compile time dependencies.
     *
     * @parameter expression="${project.compileClasspathElements}
     * @readonly
     */
    private List<String> classpathElements;

    /**
     * The location where the generated descriptor will be placed.
     *
     * @parameter expression="${project.build.outputDirectory}"
     */
    private File outputDirectory;

    /**
     * The filename of the descriptor.
     *
     * @parameter expression="META-INF/gshell/plugin.xml"
     * @required
     */
    private String fileName;

    /**
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    private MavenProject project;

    //
    // Mojo
    //

    protected URL[] getClasspath() {
        List<URL> list = new ArrayList<URL>();

        // Add the projects dependencies
        for (String filename : classpathElements) {
            try {
                list.add(new File(filename).toURI().toURL());
            }
            catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        /*
        for (Object obj : list) {
            System.err.println("    " + obj);
        }
        */

        return (URL[])list.toArray(new URL[list.size()]);
    }

    public void execute() throws MojoExecutionException {
        //
        // FIXME: This is one huge hack after another...
        //

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(classesDirectory);
        scanner.setIncludes(new String[]{"**/*.class"});
        scanner.scan();

        //
        // FIXME: Probably better to use QDox instead of this crapo...
        //

        URL[] classpath = getClasspath();
        ClassLoader parent = getClass().getClassLoader();
        ClassLoader cl = new URLClassLoader(classpath, parent);
        
        List<Class> localClasses = new ArrayList<Class>();

        for (String file : scanner.getIncludedFiles()) {
            String className = file.substring( 0, file.lastIndexOf( ".class" ) ).replace( '\\', '.' ).replace( '/', '.' );
            Class<?> c;

            try {
                c = cl.loadClass(className);
            }
            catch (ClassNotFoundException e) {
                throw new MojoExecutionException("Error scanning class " + className, e);
            }

            localClasses.add(c);
        }

        List<Class> commands = new ClassFinder(localClasses).findAnnotatedClasses(org.apache.geronimo.gshell.command.annotation.Command.class);

        Plugin plugin = new Plugin();
        plugin.setName(project.getArtifactId());
        
        for (Class type : commands) {
            org.apache.geronimo.gshell.command.annotation.Command meta = (org.apache.geronimo.gshell.command.annotation.Command)
                    type.getAnnotation(org.apache.geronimo.gshell.command.annotation.Command.class);

            Command c = new Command();

            c.setName(meta.name());
            c.setDescription(meta.description());
            c.setComment(meta.comment());
            c.setImplementation(type.getName());

            // Have to look at each class in the tree to look at its declared fields
            List<Class> classes = new ArrayList<Class>();
            while (type != null) {
                classes.add(type);
                type = type.getSuperclass();
            }
            
            for (Class t : classes) {
                for (Field f : t.getDeclaredFields()) {
                    Requirement requirement = f.getAnnotation(Requirement.class);

                    if (requirement != null) {
                        org.apache.geronimo.gshell.plugin.model.Requirement r = new org.apache.geronimo.gshell.plugin.model.Requirement();

                        if (requirement.role().isAssignableFrom(Object.class)) {
                            r.setRole(f.getType().getName());
                        }
                        else {
                            r.setRole(requirement.role().getName());
                        }

                        r.setRoleHint(requirement.hint());
                        r.setFieldName(f.getName());
                        // req.setFieldMappingType(f.getType().getName());

                        c.addRequirement(r);
                    }
                }
            }

            //
            // TODO: Configuration?
            //

            plugin.addCommand(c);
        }

        File outputFile = new File(outputDirectory, fileName);
        if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
            throw new MojoExecutionException("Cannot create directory: " + outputFile.getParent());
        }
        
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));
            PluginXpp3Writer writer = new PluginXpp3Writer();
            
            writer.write(output, plugin);
            output.close();
        }
        catch (IOException e) {
            throw new MojoExecutionException("Failed to write plugin descriptor", e);
        }
    }
}