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

import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 * Container for plugin descriptions.
 *
 * @version $Rev$ $Date$
 */
public class CommandDescriptor
    extends ComponentDescriptor
{
    private String name;

    private String description;

    private String comment;

    private Object configuration;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        assert name != null;

        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public void setCommandConfiguration(final PlexusConfiguration configuration) {
        this.configuration = configuration;
    }

    public Object getCommandConfiguration() {
        return configuration;
    }
}
