/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.mojo.minijar;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 * @goal minijar
 * @description strips unused classes from the dependencies
 * @requiresDependencyResolution compile
 * @execute phase="package"
 * */
public final class MiniJarMojo
    extends AbstractMojo
{
   /**
     * @parameter expression="${executedProject}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter
     */
    private List includes;

    protected List getIncludes()
    {
        if ( includes != null && !includes.isEmpty() )
        {
            return includes;
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }

    //
    // TODO: Add support to include specific classes, or class patterns as roots
    //
    
    /**
     * @parameter expression="${project.build.directory}/minijar"
     * @required
     */
    private File outputDirectory;

    public void execute()
        throws MojoExecutionException
    {
        final Set artifacts = project.getArtifacts();

        final Console console = new Console()
        {
            public void println( final String pString )
            {
                getLog().info( pString );
            }
        };

        try
        {
            final MiniJarProcessor processor = new MiniJarProcessor();

            List includes = getIncludes();
            getLog().info("Includes: " + includes);

            Iterator iter = includes.iterator();

            while (iter.hasNext()) {
                Include inc = (Include)iter.next();

                File dir = inc.getDirectory();
                String pattern = inc.getPattern();
                DirectoryScanner scanner = new DirectoryScanner();
                scanner.setBasedir( dir );
                scanner.setIncludes( new String[] { pattern } );
                scanner.scan();

                List includedFiles = Arrays.asList( scanner.getIncludedFiles() );
                for ( Iterator j = includedFiles.iterator(); j.hasNext(); )
                {
                    String name = (String) j.next();
                    File file = new File( dir, name );

                    getLog().info("Adding root unit: " + file);
                    processor.addRootUnit( file.getAbsolutePath() );
                }
            }

            for ( final Iterator it = artifacts.iterator(); it.hasNext(); )
            {
                final Artifact dependencyArtifact = (Artifact) it.next();
                final File file = dependencyArtifact.getFile();

                if ("jar".equals(dependencyArtifact.getType())) {
                    getLog().info("Adding unit: " + file);

                    processor.addUnit( file.getAbsolutePath() );
                }
                else {
                    getLog().info("Skipping non-jar artifact: " + dependencyArtifact);
                }
            }

            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }
            processor.setOutputPath( outputDirectory.getAbsolutePath() );

            processor.generate( console );

        }
        catch ( final IOException e )
        {
            throw new MojoExecutionException( "failed to process jar", e );
        }
    }
}
