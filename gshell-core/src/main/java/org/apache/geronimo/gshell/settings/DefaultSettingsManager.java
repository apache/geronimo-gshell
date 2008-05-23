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

import org.apache.geronimo.gshell.artifact.ArtifactManager;
import org.apache.geronimo.gshell.model.common.RemoteRepository;
import org.apache.geronimo.gshell.model.settings.Settings;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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

    private SettingsConfiguration settingsConfiguration;

    public Settings getSettings() {
        if (settingsConfiguration == null) {
            throw new IllegalStateException("Not configured");
        }

        return settingsConfiguration.getSettings();
    }

    public void configure(SettingsConfiguration config) throws Exception {
        assert config != null;

        log.debug("Configuring; config: {}", config);

        // Validate the configuration
        config.validate();
        
        Settings settings = config.getSettings();
        if (settings != null) {
            configure(settings);
        }
        
        // TODO: Merge in some default settings or something?

        settingsConfiguration = config;
    }

    private void configure(final Settings settings) throws Exception {
        assert settings != null;

        // Setup remote repositories
        for (RemoteRepository repo : settings.remoteRepositories()) {
            artifactManager.addRemoteRepository(repo.getId(), repo.getLocationUri());
        }
        
        // TODO: apply other artifact related settings (proxy, auth, whatever)
    }
}