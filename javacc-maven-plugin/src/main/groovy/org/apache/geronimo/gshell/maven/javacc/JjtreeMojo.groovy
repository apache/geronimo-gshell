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

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.shared.model.fileset.FileSet
import org.apache.maven.shared.model.fileset.util.FileSetManager

/**
 * Invokes the JJTree AST generator pre-processor.
 *
 * @goal jjtree
 *
 * @version $Id$
 */
class JjtreeMojo
    extends JavaccMojoSupport
{
    /**
     * Where to output generated sources.
     *
     * @parameter expression="${project.build.directory}/generated-sources/jjtree"
     * @required
     */
    File outputDirectory
    
    /**
     * The package for AST node classes.
     *
     * @parameter
     */
    String nodePackage
    
    /**
     * A set of grammar files to process.
     *
     * @parameter
     * @required
     */
    FileSet grammars

    void execute() {
        ant.mkdir(dir: outputDirectory)
        
        def fsm = new FileSetManager(log, log.debugEnabled)
        def includes = fsm.getIncludedFiles(grammars)
        if (includes.length == 0) {
            fail('No grammars selected')
        }
        
        // Generate sources to a temporary location, will install them after gen has occured
        def tmpOutputDir = new File("${outputDirectory}-" + System.currentTimeMillis())
        ant.mkdir(dir: tmpOutputDir)
        
        includes.each { grammar ->
            ant.java(classname: 'jjtree', fork: true) {
                classpath() {
                    pathelement(location: getPluginArtifact('net.java.dev.javacc:javacc').file)
                }
                
                arg(value: "-OUTPUT_DIRECTORY=$tmpOutputDir")
                
                if (nodePackage) {
                    arg(value: "-NODE_PACKAGE=$nodePackage")
                }
                
                arg(value: new File(grammars.directory, grammar))
            }
        }
        
        installGeneratedSources(tmpOutputDir)
        
        // Always include generate JavaCC grammars
        ant.copy(todir: outputDirectory) {
            fileset(dir: tmpOutputDir) {
                include(name: '*.jj')
            }
        }
        
        ant.delete(dir: tmpOutputDir)
        
        project.addCompileSourceRoot(outputDirectory.canonicalPath)
    }
}
