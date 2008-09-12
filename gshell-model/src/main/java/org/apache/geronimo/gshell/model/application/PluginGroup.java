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

package org.apache.geronimo.gshell.model.application;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

import org.apache.geronimo.gshell.model.common.Artifact;

/**
 * Groups plugin elements to allow artifact configuration to be shared.
 *
 * @version $Rev$ $Date$
 */
@XStreamAlias("pluginGroup")
public class PluginGroup
    extends Artifact
{
    @XStreamImplicit
    private List<Plugin> plugins;

    public List<Plugin> getPlugins() {
        if (plugins == null) {
            plugins = new ArrayList<Plugin>();
        }

        return plugins;
    }

    public void add(final Plugin plugin) {
        assert plugin != null;

        getPlugins().add(plugin);
    }

    public int size() {
        return getPlugins().size();
    }

    public boolean isEmpty() {
        return getPlugins().isEmpty();
    }

    /**
     * Link children to their parent group when deserializing.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    private Object readResolve() {
        if (!isEmpty()) {
            for (Plugin child : getPlugins()) {
                child.setPluginGroup(this);
            }
        }

        return this;
    }
}