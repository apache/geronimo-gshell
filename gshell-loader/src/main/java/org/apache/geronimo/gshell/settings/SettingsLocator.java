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

package org.apache.geronimo.gshell.settings;

import org.apache.geronimo.gshell.model.settings.Settings;
import org.apache.geronimo.gshell.model.settings.SettingsMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Locates {@link Settings} instances.
 *
 * @version $Rev$ $Date$
 */
public class SettingsLocator
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final SettingsMarshaller marshaller = new SettingsMarshaller();

    //
    // FIXME: Need to make this more robust, allow a file override/hint look in META-INF/gshell, user.home, etc.
    //

    //
    // TODO: Use builder pattern to add additonal bits to help location
    //

    public Settings locate() throws Exception {
        log.debug("Locating settings descriptor");

        //
        // TODO: For now we just ignore user settings, but should try to locate a descriptor and unmarshal
        //
        
        return null;
    }
}