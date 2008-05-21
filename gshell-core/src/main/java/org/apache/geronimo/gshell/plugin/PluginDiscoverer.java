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

package org.apache.geronimo.gshell.plugin;

import org.codehaus.plexus.component.discovery.AbstractComponentDiscoverer;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gshell.model.plugin.PluginMarshaller;
import org.apache.geronimo.gshell.model.plugin.Plugin;

import java.io.Reader;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class PluginDiscoverer
    extends AbstractComponentDiscoverer
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final PluginMarshaller marshaller = new PluginMarshaller();

    public PluginDiscoverer() {}

    protected String getComponentDescriptorLocation() {
        return "META-INF/gshell/plugin.xml";
    }

    protected ComponentSetDescriptor createComponentDescriptors(final Reader reader, final String source) throws PlexusConfigurationException {
        assert reader != null;
        assert source != null;

        log.debug("Discovered plugin: {}", source);

        Plugin plugin = marshaller.unmarshal(reader);

        // TODO: Build plexus component set descriptor

        return null;
    }
}