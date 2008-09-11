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

package org.apache.geronimo.gshell.application.settings;

import org.apache.geronimo.gshell.yarn.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.yarn.ToStringStyle;
import org.apache.geronimo.gshell.model.settings.SettingsModel;

/**
 * Container for settings configuration.
 *
 * @version $Rev$ $Date$
 */
public class SettingsConfiguration
{
    private SettingsModel model;

    public SettingsModel getModel() {
        return model;
    }

    public void setModel(final SettingsModel model) {
        this.model = model;
    }

    public void validate() {
        // Nothing ATM
    }
    
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}