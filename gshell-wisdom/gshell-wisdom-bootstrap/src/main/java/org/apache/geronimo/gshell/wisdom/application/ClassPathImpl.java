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

import org.apache.geronimo.gshell.application.ClassPath;
import org.apache.geronimo.gshell.application.model.Artifact;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * {@link ClassPath} implementation backed up by a set of artifacts.
 *
 * @version $Rev$ $Date$
 */
public class ClassPathImpl
    implements ClassPath, Serializable
{
    private static final long serialVersionUID = 1;
    
    private Collection<Artifact> artifacts;

    private transient Collection<URL> urls;

    public ClassPathImpl() {}

    public ClassPathImpl(final Collection<Artifact> artifacts) {
        assert artifacts != null;

        this.artifacts = artifacts;
    }

    public Collection<Artifact> getArtifacts() {
        assert artifacts != null;
        return Collections.unmodifiableCollection(artifacts);
    }

    public Collection<URL> getUrls() {
        if (urls == null) {
            List<URL> list = new ArrayList<URL>(artifacts.size());

            for (Artifact artifact : artifacts) {
                File file = artifact.getFile();

                if (file != null) {
                    try {
                        list.add(file.toURI().toURL());
                    }
                    catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            this.urls = Collections.unmodifiableCollection(list);
        }

        return urls;
    }
}