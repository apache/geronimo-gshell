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

import org.apache.geronimo.gshell.application.settings.Settings;
import org.apache.geronimo.gshell.event.Event;

/**
 * Event fired once settings have been configured.
 *
 * @version $Rev$ $Date$
 */
public class SettingsConfiguredEvent
    implements Event
{
    private final Settings settings;
    
    public SettingsConfiguredEvent(final Settings settings) {
        assert settings != null;

        this.settings = settings;
    }

    public Settings getSettings() {
        return settings;
    }
}