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

package org.apache.geronimo.gshell.settings;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.apache.geronimo.gshell.model.settings.Settings;
import org.apache.geronimo.gshell.model.common.SourceRepository;
import org.apache.geronimo.gshell.artifact.ArtifactManager;
import org.apache.maven.artifact.UnknownRepositoryLayoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Default implementation of the {@link SettingsManager} component.
 *
 * @version $Rev$ $Date$
 */
@Component(role=SettingsManager.class)
public class DefaultSettingsManager
    implements SettingsManager
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Requirement
    private ArtifactManager artifactManager;

    private Settings settings;

    public void setSettings(final Settings settings) {
        assert settings != null;

        this.settings = settings;
    }

    public Settings getSettings() {
        return settings;
    }

    public void configure() throws Exception {
        log.debug("Configuring");

        List<SourceRepository> sourceRepositories = settings.sourceRepositories();
        if (sourceRepositories != null) {
            for (SourceRepository repo : sourceRepositories) {
                String loc = repo.getLocation();
                URL url = new URL(loc);
                String id = url.getHost(); // FIXME: Need to expose the repo id in the model, for now assume the id is the hostname

                artifactManager.addRemoteRepository(id, url);
            }
        }
        
        // TODO: apply other artifact related settings (proxy, auth, whatever)
    }
}