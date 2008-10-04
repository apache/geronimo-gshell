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

package org.apache.geronimo.gshell.vfs.provider.meta;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractOriginatingFileProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Meta file provider.
 *
 * @version $Rev$ $Date$
 */
public class MetaFileProvider
    extends AbstractOriginatingFileProvider
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final Collection<Capability> CAPABILITIES = Collections.unmodifiableCollection(Arrays.asList(
        Capability.ATTRIBUTES,

        // TODO: All modification should be done via the registry, see about nuking these
        Capability.CREATE,
        Capability.DELETE,
        
        Capability.GET_TYPE,
        Capability.GET_LAST_MODIFIED,
        Capability.LAST_MODIFIED,
        Capability.LIST_CHILDREN,
        Capability.URI
    ));

    @Autowired
    private MetaFileDataRegistry registry;

    public Collection getCapabilities() {
        return CAPABILITIES;
    }

    protected FileSystem doCreateFileSystem(final FileName fileName, final FileSystemOptions options) throws FileSystemException {
        assert registry != null;

        MetaFileSystem fs = new MetaFileSystem(registry, fileName, options);

        log.debug("Created file system: {}", fs);

        return fs;
    }
}