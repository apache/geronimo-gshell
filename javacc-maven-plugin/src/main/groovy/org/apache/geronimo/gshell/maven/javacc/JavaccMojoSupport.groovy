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

package org.apache.geronimo.gshell.maven.javacc

import org.codehaus.groovy.maven.mojo.GroovyMojo

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.project.MavenProject
import org.apache.maven.artifact.Artifact

import com.thoughtworks.qdox.JavaDocBuilder

/**
 * Provides support for JavaCC mojos.
 *
 * @version $Id$
 */
abstract class JavaccMojoSupport
    extends GroovyMojo
{
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    MavenProject project
    
    /**
     * @parameter expression="${plugin.artifactMap}"
     * @required
     * @readonly
     */
    Map pluginArtifactMap
    
    protected Artifact getPluginArtifact(final String name) throws MojoExecutionException {
        assert name
        
        def artifact = pluginArtifactMap.get(name)
        if (artifact == null) {
            fail("Unable to locate '${name}' in the list of plugin artifacts")
        }

        return artifact
    }
    
    /**
     * Install generated sources which have not been overridden.
     */
    protected void installGeneratedSources(File sourceDir) {
        assert sourceDir
        
        // Discover which classes were generated
        def builder = new JavaDocBuilder()
        builder.addSourceTree(sourceDir)
        
        // Install generated classes which were not overridden in some source root
        ant.mkdir(dir: outputDirectory)
        ant.copy(todir: outputDirectory) {
            // Check source roots for overrides, only copy if not found
            builder.classes.each { clazz ->
                def filepath = "${clazz.getPackage()}/${clazz.name}".replace('.', '/') + '.java'
                
                for (String sourceRoot in project.compileSourceRoots) {
                    def dir = new File(sourceRoot)
                    def file = new File(dir, filepath)
                    
                    if (file.exists()) {
                        log.info("Omitting $clazz.name, already exists in source tree")
                    }
                    else {
                        fileset(file: clazz.source.file)
                    }
                }
            }
        }
    }
}

