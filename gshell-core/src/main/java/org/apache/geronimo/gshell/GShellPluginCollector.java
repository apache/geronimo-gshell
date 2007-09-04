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

package org.apache.geronimo.gshell;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryEvent;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryListener;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hook to get some details about the Plexus components which are loaded.
 * 
 * @version $Rev$ $Date$
 */
@Component(role=GShellPluginCollector.class)
public class GShellPluginCollector
    implements ComponentDiscoveryListener
{
    public static final String ID = "gshell-plugin-collector";

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    public String getId() {
        return ID;
    }
    
    public void componentDiscovered(final ComponentDiscoveryEvent event) {
        assert event != null;

        ComponentSetDescriptor desc = event.getComponentSetDescriptor();

        log.debug("Component(s) discovered: {}", desc);
    }
}