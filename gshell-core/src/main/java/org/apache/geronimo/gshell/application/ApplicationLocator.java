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

import org.apache.geronimo.gshell.model.application.Application;
import org.apache.geronimo.gshell.model.application.ApplicationMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Locates {@link Application} instances.
 *
 * @version $Rev$ $Date$
 */
public class ApplicationLocator
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ApplicationMarshaller marshaller = new ApplicationMarshaller();

    //
    // FIXME: Need to make this more robust, allow a file override/hint look in META-INF/gshell, etc.
    //

    //
    // TODO: Use builder pattern to add additonal bits to help location
    //
    
    public Application locate() throws Exception {
        log.debug("Locating");

        URL url = getClass().getClassLoader().getResource("application.xml");

        log.debug("Application descriptor URL: {}", url);
        
        return marshaller.unmarshal(url);
    }
}