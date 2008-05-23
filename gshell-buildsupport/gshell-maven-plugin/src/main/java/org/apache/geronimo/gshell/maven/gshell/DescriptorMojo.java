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
import java.util.ArrayList;
import java.util.List;

import org.apache.geronimo.gshell.model.command.Command;
import org.apache.geronimo.gshell.model.command.CommandSet;
import org.apache.geronimo.gshell.model.command.CommandSetMarshaller;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

import org.apache.geronimo.gshell.maven.gshell.CommandDescriptorExtractor.Scope;

/**
 * Generates a GShell <tt>commands.xml</tt> descriptor.
 *
 * @goal descriptor
 * @phase process-classes
 * @requiresDependencyResolution runtime
 *
 * @version $Rev$ $Date$
 */
public class DescriptorMojo
    extends AbstractMojo
{
    /**
     * The directory where the descriptor is written.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
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

    public void execute() throws MojoExecutionException {
        // Only execute if the current project looks like its got Java bits in it
        ArtifactHandler artifactHandler = project.getArtifact().getArtifactHandler();

        if (!"java".equals(artifactHandler.getLanguage())) {
            getLog().debug("Not executing on non-Java project");
        }
        else {
            File outputFile = new File(outputDirectory, fileName);

            generateDescriptor(Scope.COMPILE, outputFile);
        }
    }

    protected void generateDescriptor(final Scope scope, final File outputFile) throws MojoExecutionException {
        assert scope != null;
        assert outputFile != null;

        List<Command> descriptors = new ArrayList<Command>();

        CommandDescriptorExtractor extractor = new CommandDescriptorExtractor();

        try {
            List<Command> list = extractor.extract(project, scope);

            if (list != null && !list.isEmpty()) {
                descriptors.addAll(list);
            }
        }
        catch (Exception e) {
            throw new MojoExecutionException("Failed to extract descriptors", e);
        }

        if (descriptors.size() == 0) {
            getLog().debug("No commands found");
        }
        else {
            getLog().info("Discovered " + descriptors.size() + " command descriptors(s)");

            CommandSet commands = new CommandSet(project.getId()); // .getArtifactId());
            commands.setCommands(descriptors);

            try {
                writeDescriptor(commands, outputFile);
            }
            catch (Exception e) {
                throw new MojoExecutionException("Failed to write descriptor: " + outputFile, e);
            }
        }
    }

    private void writeDescriptor(final CommandSet commands, final File outputFile) throws Exception {
        assert commands != null;
        assert outputFile != null;

        FileUtils.forceMkdir(outputFile.getParentFile());

        BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));

        CommandSetMarshaller marshaller = new CommandSetMarshaller();

        try {
            marshaller.marshal(commands, output);
            output.flush();
        }
        finally {
            IOUtil.close(output);
        }

        getLog().debug("Wrote: " + outputFile);
    }
}