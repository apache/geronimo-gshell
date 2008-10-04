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

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link MetaFileDataRegistry} component.
 *
 * @version $Rev$ $Date$
 */
public class  MetaFileDataRegistryImpl
    implements MetaFileDataRegistry
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Map<FileName,MetaFileData> nodes = /*Collections.synchronizedMap(*/new HashMap<FileName,MetaFileData>()/*)*/;

    private String rootFileName = MetaFileName.SCHEME + ":/";

    @PostConstruct
    public void init() throws FileSystemException {
        // Register the root folder
        MetaFileNameParser parser = new MetaFileNameParser();
        FileName rootName = parser.parseUri(rootFileName);
        registerData(rootName, new MetaFileData(rootName, FileType.FOLDER));
    }

    protected Map<FileName,MetaFileData> getNodes() {
        return nodes;
    }
    
    public void registerData(final FileName name, final MetaFileData data) {
        assert name != null;
        assert data != null;

        log.debug("Registering data: {} -> {}", name, data);

        if (name.getDepth() > 0) {
            FileName parentName = name.getParent();
            if (containsData(parentName)) {
                MetaFileData parent = lookupData(parentName);

                if (!parent.hasChild(data)) {
                    try {
                        parent.addChild(data);
                    }
                    catch (FileSystemException ignore) {
                        throw new Error(ignore);
                    }
                }
            }
            else {
                log.warn("Missing parent folder: " + parentName);
            }
        }

        getNodes().put(name, data);
    }

    public void removeData(final FileName name) {
        assert name != null;

        log.debug("Removing data: {}", name);

        MetaFileData data = getNodes().remove(name);

        FileName parentName = name.getParent();
        if (containsData(parentName)) {
            MetaFileData parent = lookupData(parentName);
            try {
                parent.removeChild(data);
            }
            catch (FileSystemException ignore) {
                throw new Error(ignore);
            }
        }
    }

    public boolean containsData(final FileName name) {
        assert name != null;

        return getNodes().containsKey(name);
    }

    public MetaFileData lookupData(final FileName name) {
        assert name != null;

        log.debug("Looking up data: {}", name);
        
        return getNodes().get(name);
    }
}