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

package org.apache.geronimo.gshell.vfs;

import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.Capability;
import org.springframework.beans.factory.FactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Sprint {@link FactoryBean} to construct the {@link FileSystemManager} instance.
 *
 * @version $Rev$ $Date$
 */
public class FileSystemManagerFactoryBean
    implements FactoryBean
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    public Object getObject() throws Exception {
        //
        // TODO: Manually construct the FSM stuff, don't use the default
        //
        
        FileSystemManager fsm = VFS.getManager();

        log.debug("File system manager: {}", fsm);
        
        // Dump some details about the current configuration
        if (log.isTraceEnabled()) {
            log.trace("    Cache strategy: {}", fsm.getCacheStrategy());
            log.trace("    File content info factory: {}", fsm.getFileContentInfoFactory());

            //
            // TODO: Add commands to inspect all this muck and install new providers, etc.
            //
            
            log.trace("Schemes:");
            String[] schemes = fsm.getSchemes();
            for (String scheme : schemes) {
                log.trace("    {}", scheme);

                // noinspection unchecked
                Collection<Capability> capabilities = fsm.getProviderCapabilities(scheme);
                if (!capabilities.isEmpty()) {
                    for (Capability capability : capabilities) {
                        log.trace("        {}", capability);
                    }
                }
            }
        }

        return fsm;
    }

    public Class getObjectType() {
        return FileSystemManager.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
