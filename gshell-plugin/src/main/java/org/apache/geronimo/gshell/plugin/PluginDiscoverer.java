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

import java.io.Reader;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.discovery.AbstractComponentDiscoverer;
import org.codehaus.plexus.component.discovery.ComponentDiscoverer;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Plexus component discovery component to handle the GShell plugin.xml muck.
 *
 * @version $Rev$ $Date$
 */
@Component(role= ComponentDiscoverer.class, hint="gshell")
public class PluginDiscoverer
    extends AbstractComponentDiscoverer
{
    public static final String PLUGIN_XML_LOCATION = "META-INF/gshell/plugin.xml";

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final PluginDescriptorBuilder builder = new PluginDescriptorBuilder();

    public String getComponentDescriptorLocation() {
        return PLUGIN_XML_LOCATION;
    }

    public ComponentSetDescriptor createComponentDescriptors(final Reader reader, final String source)
        throws PlexusConfigurationException
    {
        log.debug("Creating components from: {}", source);
        
        return builder.build(reader, source);
    }
}