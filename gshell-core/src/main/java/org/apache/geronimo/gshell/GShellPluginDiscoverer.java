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

package org.apache.geronimo.gshell;

import java.io.Reader;

import org.codehaus.plexus.component.discovery.DefaultComponentDiscoverer;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class GShellPluginDiscoverer
    extends DefaultComponentDiscoverer// AbstractComponentDiscoverer
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    /*
    private PluginDescriptorBuilder builder;

    public MavenPluginDiscoverer()
    {
        builder = new PluginDescriptorBuilder();
    }

    public String getComponentDescriptorLocation()
    {
        return "META-INF/maven/plugin.xml";
    }

    public ComponentSetDescriptor createComponentDescriptors( Reader componentDescriptorConfiguration, String source )
        throws PlexusConfigurationException
    {
        return builder.build( componentDescriptorConfiguration, source );
    }
    */

    /*
    protected String getComponentDescriptorLocation() {
        return null;
    }

    protected ComponentSetDescriptor createComponentDescriptors(Reader reader, String source) throws PlexusConfigurationException {
        return null;
    }
    */

    /*
    This is the default bits...

    public String getComponentDescriptorLocation()
    {
        return "META-INF/plexus/components.xml";
    }

    public ComponentSetDescriptor createComponentDescriptors( Reader componentDescriptorReader, String source )
        throws PlexusConfigurationException
    {
        PlexusConfiguration componentDescriptorConfiguration = PlexusTools.buildConfiguration( source, componentDescriptorReader );

        ComponentSetDescriptor componentSetDescriptor = new ComponentSetDescriptor();

        List componentDescriptors = new ArrayList();

        PlexusConfiguration[] componentConfigurations =
            componentDescriptorConfiguration.getChild( "components" ).getChildren( "component" );

        for ( int i = 0; i < componentConfigurations.length; i++ )
        {
            PlexusConfiguration componentConfiguration = componentConfigurations[i];

            ComponentDescriptor componentDescriptor;

            try
            {
                componentDescriptor = PlexusTools.buildComponentDescriptor( componentConfiguration );
            }
            catch ( PlexusConfigurationException e )
            {
                throw new PlexusConfigurationException( "Cannot process component descriptor: " + source, e );
            }

            componentDescriptor.setSource( source );
            componentDescriptor.setComponentType( "plexus" );

            componentDescriptors.add( componentDescriptor );
        }

        componentSetDescriptor.setComponents( componentDescriptors );

        // TODO: read and store the dependencies

        return componentSetDescriptor;
    }
    */
}