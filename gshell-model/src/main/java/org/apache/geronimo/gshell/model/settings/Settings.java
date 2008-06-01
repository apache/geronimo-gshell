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

package org.apache.geronimo.gshell.model.settings;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import org.apache.geronimo.gshell.model.Model;
import org.apache.geronimo.gshell.model.common.RemoteRepository;

import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

/**
 * User settings model root element.
 *
 * @version $Rev$ $Date$
 */
@XStreamAlias("settings")
public class Settings
    extends Model
{
    private Properties properties;

    // TODO: Proxies

    private List<RemoteRepository> remoteRepositories;

    // TODO: Repository

    // TODO: Paths

    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
        }

        return properties;
    }

    public void setProperties(final Properties properties) {
        this.properties = properties;
    }

    public List<RemoteRepository> remoteRepositories() {
        if (remoteRepositories == null) {
            remoteRepositories = new ArrayList<RemoteRepository>();
        }

        return remoteRepositories;
    }

    public void add(final RemoteRepository repository) {
        assert repository != null;

        remoteRepositories().add(repository);
    }
}