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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.command.descriptor.CommandDescriptor;
import org.apache.geronimo.gshell.command.descriptor.CommandSetDescriptor;
import org.apache.geronimo.gshell.command.descriptor.CommandSetDescriptorWriter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.xbean.finder.ClassFinder;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.IOUtil;

/**
 * Generates a GShell XML commands descriptor.
 *
 * @version $Rev$ $Date$
 * @goal commands-descriptor
 * @phase process-classes
 * @requiresDependencyResolution runtime
 */
public class CommandSetDescriptorMojo
        extends AbstractMojo {
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
     * @parameter expression="META-INF/gshell/commands.xml"
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

        getLog().debug("Classpath:");
        for (Object obj : list) {
            getLog().debug("    " + obj);
        }

        return list.toArray(new URL[list.size()]);
    }

    private List<Class> loadClasses(final ClassLoader cl) throws MojoExecutionException {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(classesDirectory);
        scanner.setIncludes(new String[]{"**/*.class"});
        scanner.scan();

        List<Class> classes = new ArrayList<Class>();

        for (String file : scanner.getIncludedFiles()) {
            String className = file.substring(0, file.lastIndexOf(".class")).replace('\\', '.').replace('/', '.');
            Class<?> c;

            try {
                c = cl.loadClass(className);
            }
            catch (ClassNotFoundException e) {
                throw new MojoExecutionException("Failed to load class: " + className, e);
            }

            classes.add(c);
        }

        return classes;
    }

    private List<Class> getClasses(Class<?> type) {
        assert type != null;

        List<Class> classes = new ArrayList<Class>();

        while (type != null) {
            classes.add(type);
            type = type.getSuperclass();
        }

        return classes;
    }

    private CommandDescriptor createCommandDescriptor(final Class<?> type) {
        getLog().info("Found command: " + type);

        CommandDescriptor desc = new CommandDescriptor();

        CommandComponent anno = type.getAnnotation(CommandComponent.class);

        desc.setId(anno.id());
        desc.setDescription(anno.description());
        desc.setImplementation(type.getName());

        // Have to look at each class in the tree to look at its declared fields
        List<Class> classes = getClasses(type);

        for (Class t : classes) {
            for (Field f : t.getDeclaredFields()) {
                Requirement requirementAnno = f.getAnnotation(Requirement.class);

                if (requirementAnno != null) {
                    ComponentRequirement requirement = new ComponentRequirement();

                    if (requirementAnno.role().isAssignableFrom(Object.class)) {
                        requirement.setRole(f.getType().getName());
                    } else {
                        requirement.setRole(requirementAnno.role().getName());
                    }

                    requirement.setRoleHint(requirementAnno.hint());
                    requirement.setFieldName(f.getName());
                    requirement.setFieldMappingType(f.getType().getName());

                    getLog().debug("Found requirement: " + requirement);

                    desc.addRequirement(requirement);
                }

                Configuration configAnno = f.getAnnotation(Configuration.class);

                if (configAnno != null) {
                    PlexusConfiguration config = null; // new PlexusConfiguration();

                    //
                    // TODO: Convert the annoation to a PlexusConfiguration
                    //

                    getLog().debug("Found configuration: " + config);

                    desc.setConfiguration(config);
                }
            }
        }

        return desc;
    }

    public void execute() throws MojoExecutionException {
        getLog().debug("Attempting to discover comamnds...");

        ClassLoader cl = new URLClassLoader(getClasspath(), getClass().getClassLoader());

        List<Class> classes = loadClasses(cl);

        List<Class> commands = new ClassFinder(classes).findAnnotatedClasses(CommandComponent.class);

        // Stip off any commands which aren't public or are abstract
        for (Class type : commands) {
            int mod = type.getModifiers();

            if (!Modifier.isPublic(mod) || Modifier.isAbstract(mod)) {
                commands.remove(type);
            }
        }

        if (commands.size() > 0) {
            getLog().info("Discovered " + commands.size() + " command type(s)");

            CommandSetDescriptor setDesc = new CommandSetDescriptor();
            setDesc.setId(project.getArtifactId());

            for (Class type : commands) {
                CommandDescriptor commandDesc = createCommandDescriptor(type);
                setDesc.addCommandDescriptor(commandDesc);
            }

            File outputFile = new File(outputDirectory, fileName);
            if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
                throw new MojoExecutionException("Cannot create directory: " + outputFile.getParent());
            }

            // Write the file

            BufferedWriter output = null;
            try {
                output = new BufferedWriter(new FileWriter(outputFile));
                CommandSetDescriptorWriter writer = new CommandSetDescriptorWriter();

                writer.write(output, setDesc);

                getLog().debug("Wrote " + outputFile);
            }
            catch (Exception e) {
                throw new MojoExecutionException("Failed to write commands descriptor", e);
            }
            finally {
                IOUtil.close(output);
            }
        }
    }
}