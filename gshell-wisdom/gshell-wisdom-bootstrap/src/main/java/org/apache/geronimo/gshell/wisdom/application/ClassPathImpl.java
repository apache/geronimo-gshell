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

package org.apache.geronimo.gshell.wisdom.application;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.geronimo.gshell.application.ClassPath;
import org.apache.geronimo.gshell.model.common.Artifact;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@link ClassPath} implementation backed up by a set of Maven artifacts.
 *
 * @version $Rev$ $Date$
 */
@XStreamAlias("classpath")
public class ClassPathImpl
    implements ClassPath
{
    private Collection<URL> urls;

    private Collection<Artifact> artifacts;

    public ClassPathImpl() {}
    
    public ClassPathImpl(final Set<org.apache.maven.artifact.Artifact> artifacts) {
        assert artifacts != null;

        this.artifacts = new LinkedHashSet<Artifact>();
        this.urls = new LinkedHashSet<URL>();

        for (org.apache.maven.artifact.Artifact source : artifacts) {
            Artifact artifact = new Artifact();
            artifact.setGroupId(source.getGroupId());
            artifact.setArtifactId(source.getArtifactId());
            artifact.setType(source.getType());
            artifact.setVersion(source.getVersion());

            try {
                File file = source.getFile();
                if (file == null) {
                    throw new IllegalStateException("Artifact missing resolved local-file: " + source);
                }

                //
                // TODO: Need to make this handle when ${gshell.home} is moved, so we can still re-use the cache.
                //       Also need to, when unmarshalling, validate that the files still exist and invalidate if not
                //
                
                URL url = file.toURI().toURL();
                this.urls.add(url);
            }
            catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }

            this.artifacts.add(artifact);
        }
    }

    public Collection<URL> getUrls() {
        assert urls != null;
        return Collections.unmodifiableCollection(urls);
    }

    public Collection<Artifact> getArtifacts() {
        assert artifacts != null;
        return Collections.unmodifiableCollection(artifacts);
    }
}