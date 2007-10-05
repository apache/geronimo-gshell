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

package org.apache.geronimo.gshell.plugin.adapter;

import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.descriptor.CommandConfiguration;
import org.apache.geronimo.gshell.descriptor.CommandConfigurationException;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class PlexusConfigurationAdapter
    implements PlexusConfiguration
{
    private final CommandConfiguration configuration;

    public PlexusConfigurationAdapter(final CommandConfiguration configuration) {
        assert configuration != null;

        this.configuration = configuration;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public CommandConfiguration getConfiguration() {
        return configuration;
    }

    //
    // PlexusConfiguration
    //

    public String getName() {
        return configuration.getName();
    }

    public String getValue() throws PlexusConfigurationException {
        try {
            return configuration.getValue();
        }
        catch (CommandConfigurationException e) {
            throw new PlexusConfigurationException(e.getMessage(), e);
        }
    }

    public String getValue(final String defaultValue) {
        return configuration.getValue(defaultValue);
    }

    //
    // FIXME: Finish...
    //

    public String[] getAttributeNames() {
        return new String[0];
    }

    public String getAttribute(final String name) throws PlexusConfigurationException {
        return null;
    }

    public String getAttribute(final String name, final String defaultValue) {
        return null;
    }

    public PlexusConfiguration getChild(final String name) {
        return null;
    }

    public PlexusConfiguration getChild(final int i) {
        return null;
    }

    public PlexusConfiguration getChild(final String name, final boolean create) {
        return null;
    }

    public PlexusConfiguration[] getChildren() {
        return new PlexusConfiguration[0];
    }

    public PlexusConfiguration[] getChildren(final String name) {
        return new PlexusConfiguration[0];
    }

    public void addChild(final PlexusConfiguration child) {
        return;
    }

    public int getChildCount() {
        return 0;
    }
}