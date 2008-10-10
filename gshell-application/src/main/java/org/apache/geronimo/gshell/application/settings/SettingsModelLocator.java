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

import org.apache.geronimo.gshell.model.settings.SettingsModel;
import org.apache.geronimo.gshell.model.settings.SettingsModelMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.java2d.loops.GraphicsPrimitiveMgr;

import java.util.List;
import java.util.ArrayList;

/**
 * Locates {@link SettingsModel} instances.
 *
 * @version $Rev$ $Date$
 */
public class SettingsModelLocator
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final SettingsModelMarshaller marshaller = new SettingsModelMarshaller();

    private final List<String> locations = new ArrayList<String>();

    //
    // FIXME: Need to make this more robust, allow a file override/hint look in META-INF/gshell, user.home, etc.
    //

    public SettingsModelLocator addLocation(final String location) {
        if (location != null) {
            log.debug("Adding location: {}", location);
        }

        // TODO:
        
        return this;
    }

    public SettingsModel locate() throws Exception {
        log.debug("Locating settings model descriptor");

        //
        // TODO: For now we just ignore user settings, but should try to locate a descriptor and unmarshal
        //
        
        return null;
    }
}