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

import org.apache.geronimo.gshell.application.settings.SettingsConfiguration;
import org.apache.geronimo.gshell.application.settings.SettingsManager;
import org.apache.geronimo.gshell.application.settings.Settings;
import org.apache.geronimo.gshell.artifact.ArtifactManager;
import org.apache.geronimo.gshell.model.common.RemoteRepository;
import org.apache.geronimo.gshell.model.interpolate.Interpolator;
import org.apache.geronimo.gshell.model.interpolate.InterpolatorSupport;
import org.apache.geronimo.gshell.model.settings.SettingsModel;
import org.apache.geronimo.gshell.spring.BeanContainerAware;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.wisdom.application.event.SettingsConfiguredEvent;
import org.codehaus.plexus.interpolation.PropertiesBasedValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of the {@link SettingsManager} component.
 *
 * @version $Rev$ $Date$
 */
public class SettingsManagerImpl
    implements SettingsManager, BeanContainerAware
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ArtifactManager artifactManager;

    private SettingsConfiguration settingsConfiguration;

    private Settings settings;

    private BeanContainer container;

    public void setBeanContainer(final BeanContainer container) {
        assert container != null;

        this.container = container;
    }

    public Settings getSettings() {
        if (settings == null) {
            throw new IllegalStateException("Not configured");
        }

        return settings;
    }

    public void configure(final SettingsConfiguration config) throws Exception {
        assert config != null;

        log.trace("Configuring; config: {}", config);

        // Validate the configuration
        config.validate();

        if (config.getModel() != null) {
	        // Interpolate the model
	        interpolate(config);

	        // Configure settings
            configure(config.getModel());
        }

        // TODO: Merge in some default settings or something?

        settingsConfiguration = config;

        settings = new Settings()
        {
            public SettingsModel getModel() {
                return settingsConfiguration.getModel();
            }
        };

        log.debug("Settings configured");

        container.publish(new SettingsConfiguredEvent(this));
    }

    private void interpolate(final SettingsConfiguration config) throws Exception {
    	assert config != null;

    	SettingsModel model = config.getModel();
        Interpolator<SettingsModel> interp = new InterpolatorSupport<SettingsModel>();

        // Add value sources to resolve muck
        interp.addValueSource(new PropertiesBasedValueSource(System.getProperties()));
        interp.addValueSource(new PropertiesBasedValueSource(model.getProperties()));

        model = interp.interpolate(model);

        // Update the configuration with the new model
        config.setModel(model);
    }

    private void configure(final SettingsModel settingsModel) throws Exception {
        assert settingsModel != null;

        // TODO: Add settings interpolation here

        // Setup remote repositories
        for (RemoteRepository repo : settingsModel.remoteRepositories()) {
            artifactManager.getRepositoryManager().addRemoteRepository(repo.getId(), repo.getLocationUri());
        }

        // TODO: apply other artifact related settings (proxy, auth, whatever)
    }
}