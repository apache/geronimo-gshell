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

package org.apache.geronimo.gshell.application;

import org.apache.geronimo.gshell.model.application.ApplicationModel;
import org.apache.geronimo.gshell.model.application.ApplicationModelMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;

/**
 * Locates {@link ApplicationModel} instances.
 *
 * @version $Rev$ $Date$
 */
public class ApplicationModelLocator
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ApplicationModelMarshaller marshaller = new ApplicationModelMarshaller();

    private final List<String> locations = new ArrayList<String>();

    //
    // FIXME: Need to make this more robust, allow a file override/hint look in META-INF/gshell, etc.
    //

    public ApplicationModelLocator addLocation(final String location) {
        if (location != null) {
            log.debug("Adding location: {}", location);
        }

        locations.add(location);

        return this;
    }

    public ApplicationModel locate() throws Exception {
        log.debug("Locating application model descriptor");

        // TODO: look for locations, based on reverse view of locations list

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource("application.xml");

        if (url == null) {
            throw new RuntimeException("Unable to locate application model descriptor");
        }

        log.debug("Application model descriptor URL: {}", url);
        
        return marshaller.unmarshal(url);
    }
}