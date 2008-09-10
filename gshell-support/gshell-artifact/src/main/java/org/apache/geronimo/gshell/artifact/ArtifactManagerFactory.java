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

package org.apache.geronimo.gshell.artifact;

import org.springframework.beans.factory.FactoryBean;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

/**
 * Creates {@link ArtifactManager} beans.
 *
 * @version $Rev$ $Date$
 */
public class ArtifactManagerFactory
    implements FactoryBean
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private PlexusContainer container;

    @PostConstruct
    public void init() throws Exception {
        DefaultContainerConfiguration config = new DefaultContainerConfiguration();

        ClassWorld classWorld = new ClassWorld();
        config.setClassWorld(classWorld);

        ClassRealm classRealm = classWorld.newRealm("gshell.artifact", getClass().getClassLoader());
        config.setRealm(classRealm);

        container = new DefaultPlexusContainer(config);

        log.debug("Constructed Plexus container: {}", container);
    }
    
    public Object getObject() throws Exception {
        if (container == null) {
            container = new DefaultPlexusContainer();
        }

        Object target = container.lookup(ArtifactManager.class);

        log.debug("Using ArtifactManager: {}", target);

        return target;
    }

    public Class getObjectType() {
        return ArtifactManager.class;
    }

    public boolean isSingleton() {
        return true;
    }
}