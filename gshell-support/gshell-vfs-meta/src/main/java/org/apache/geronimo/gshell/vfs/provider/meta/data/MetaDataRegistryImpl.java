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

package org.apache.geronimo.gshell.vfs.provider.meta.data;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.geronimo.gshell.vfs.provider.meta.MetaFileName;
import org.apache.geronimo.gshell.vfs.provider.meta.MetaFileNameParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link MetaDataRegistry} component.
 *
 * @version $Rev$ $Date$
 */
public class MetaDataRegistryImpl
    implements MetaDataRegistry
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Map<FileName, MetaData> nodes = new HashMap<FileName, MetaData>();

    private String rootFileName = MetaFileName.SCHEME + ":/";

    @PostConstruct
    public void init() throws FileSystemException {
        // Register the root folder
        MetaFileNameParser parser = new MetaFileNameParser();
        FileName rootName = parser.parseUri(rootFileName);
        registerData(rootName, new MetaData(rootName, FileType.FOLDER));
    }

    protected Map<FileName, MetaData> getNodes() {
        return nodes;
    }
    
    public void registerData(final FileName name, final MetaData data) {
        assert name != null;
        assert data != null;

        log.trace("Registering data: {}", name);

        if (name.getDepth() > 0) {
            MetaData parent = getParentData(name);
            if (parent != null) {
                if (!parent.hasChild(data)) {
                    parent.addChild(data);
                }
            }
            else {
                log.warn("Depth is > 0, but parent name is null for node: " + name);
            }
        }

        getNodes().put(name, data);
    }

    private MetaData getParentData(final FileName name) {
        assert name != null;

        FileName parentName = name.getParent();
        MetaData parent = null;

        if (parentName != null) {
            if (containsData(parentName)) {
                parent = lookupData(parentName);
            }
            else {
                log.debug("Building parent tree: {}", parentName);
                parent = new MetaData(parentName, FileType.FOLDER);
                registerData(parentName, parent);
            }
        }

        return parent;
    }

    public void removeData(final FileName name) {
        assert name != null;

        log.trace("Removing data: {}", name);

        MetaData data = getNodes().remove(name);

        FileName parentName = name.getParent();
        if (parentName != null && containsData(parentName)) {
            MetaData parent = lookupData(parentName);
            parent.removeChild(data);
        }
    }

    public boolean containsData(final FileName name) {
        assert name != null;

        return getNodes().containsKey(name);
    }

    public MetaData lookupData(final FileName name) {
        assert name != null;

        log.trace("Looking up data: {}", name);
        
        if (!containsData(name)) {
            //
            // TODO: Maybe reconsider just returning null, not sure the exception sipmlifies anything
            //
            throw new MetaDataRegistryException("No data registered for: " + name);
        }

        return getNodes().get(name);
    }
}