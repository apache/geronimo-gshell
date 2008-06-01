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
import org.apache.geronimo.gshell.model.interpolate.Interpolator;
import org.apache.geronimo.gshell.model.interpolate.InterpolatorSupport;
import org.apache.geronimo.gshell.model.settings.Settings;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.interpolation.PropertiesBasedValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public void configure(final SettingsConfiguration config) throws Exception {
        assert config != null;

        log.trace("Configuring; config: {}", config);

        // Validate the configuration
        config.validate();
        
        if (config.getSettings() != null) {
	        // Interpolate the model
	        interpolate(config);

	        // Configure settings
            configure(config.getSettings());
        }
        
        // TODO: Merge in some default settings or something?

        settingsConfiguration = config;
    }

    private void interpolate(final SettingsConfiguration config) throws Exception {
    	assert config != null;
    	
    	Settings settings = config.getSettings();
        Interpolator<Settings> interp = new InterpolatorSupport<Settings>();

        // Add value sources to resolve muck
        interp.addValueSource(new PropertiesBasedValueSource(System.getProperties()));
        
        //
        // TODO: Add more
        //
        
        settings = interp.interpolate(settings);
        
        // Update the configuration with the new model
        config.setSettings(settings);
    }
    
    private void configure(final Settings settings) throws Exception {
        assert settings != null;

        // TODO: Add settings interpolation here
        
        // Setup remote repositories
        for (RemoteRepository repo : settings.remoteRepositories()) {
            artifactManager.getRepositoryManager().addRemoteRepository(repo.getId(), repo.getLocationUri());
        }
        
        // TODO: apply other artifact related settings (proxy, auth, whatever)
    }
}