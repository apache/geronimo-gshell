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

package org.apache.geronimo.gshell.commands.bsf;

import org.springframework.beans.factory.FactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.bsf.BSFManager;

import java.util.Map;

/**
 * Spring {@link FactoryBean} to construct a {@link BSFManager} instance.
 *
 * @version $Rev$ $Date$
 */
public class BSFManagerFactoryBean
    implements FactoryBean
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    public Object getObject() throws Exception {
        AccessibleBSFManager manager = new AccessibleBSFManager();

        log.debug("BSF manager: {}", manager);

        Map<String,String> engines = manager.getRegisteredEngines();

        //
        // NOTE: This is mostly useless debugging as it only shows the map of engines that could be used
        //       not engines that are actually available (ie. have impl classes and such)
        //

        if (engines == null || engines.isEmpty()) {
            log.warn("No engines have been registered");
        }
        else {
            log.debug("Detected {} engines:", engines.size());
            
            for (String language : engines.keySet()) {
                log.debug("    {} -> {}", language, engines.get(language));
            }
        }

        return manager;
    }

    public Class getObjectType() {
        return BSFManager.class;
    }

    public boolean isSingleton() {
        return false;
    }

    /**
     * Helper to expose some internal details of the BSFManager.
     */
    private static class AccessibleBSFManager
        extends BSFManager
    {
        @SuppressWarnings({"unchecked"})
        public Map<String,String> getRegisteredEngines() {
            return registeredEngines;
        }
    }
}
