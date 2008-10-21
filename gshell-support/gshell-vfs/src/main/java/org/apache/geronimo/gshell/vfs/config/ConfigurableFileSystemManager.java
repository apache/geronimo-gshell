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

package org.apache.geronimo.gshell.vfs.config;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.NameScope;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configurable {@link FileSystemManager}.
 *
 * @version $Rev$ $Date$
 */
public class ConfigurableFileSystemManager
    extends DefaultFileSystemManager
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    //
    // TODO: Consider implementing a way to dynamically load providers for a given scheme when requested
    //
    
    @Override
    public FileName resolveName(final FileName base, final String name, final NameScope scope) throws FileSystemException {
        if (log.isTraceEnabled()) {
            log.trace("Resolving name; base={}, name={}, scope={}", new Object[] { base, name, scope });
        }

        return super.resolveName(base, name, scope);
    }
}