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

package org.apache.geronimo.gshell.rapture.plugin;

import org.apache.geronimo.gshell.model.plugin.Plugin;
import org.apache.geronimo.gshell.model.plugin.PluginMarshaller;
import org.apache.geronimo.gshell.rapture.descriptor.PluginDescriptor;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.discovery.AbstractComponentDiscoverer;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;

/**
 * Handles the discovery of GShell plugins from <tt>plugin.xml</tt> models.
 *
 * @version $Rev$ $Date$
 */
@Component(role=PluginDiscoverer.class)
public class PluginDiscoverer
    extends AbstractComponentDiscoverer
{
    public static final String DESCRIPTOR_LOCATION = "META-INF/gshell/plugin.xml";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final PluginMarshaller marshaller = new PluginMarshaller();

    public PluginDiscoverer() {}

    protected String getComponentDescriptorLocation() {
        return DESCRIPTOR_LOCATION;
    }

    protected ComponentSetDescriptor createComponentDescriptors(final Reader reader, final String source) throws PlexusConfigurationException {
        assert reader != null;
        assert source != null;

        log.trace("Loading plugin model from: {}", source);

        Plugin plugin = marshaller.unmarshal(reader);

        log.debug("Discovered plugin: {}", plugin.getId());
        log.trace("Plugin model: {}", plugin);

        PluginDescriptor descriptor = new PluginDescriptor(plugin);
        descriptor.setSource(source);

        return descriptor;
    }
}