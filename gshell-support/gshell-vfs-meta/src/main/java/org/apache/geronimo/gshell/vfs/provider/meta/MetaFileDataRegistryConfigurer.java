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

/**
 * Helper to configure the contents of a {@link MetaFileDataRegistry}.
 *
 * @version $Rev$ $Date$
 */
public class MetaFileDataRegistryConfigurer
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final MetaFileDataRegistry registry;

    private final MetaFileNameParser nameParser;

    public MetaFileDataRegistryConfigurer(final MetaFileDataRegistry registry) {
        assert registry != null;

        this.registry = registry;
        this.nameParser = new MetaFileNameParser();
    }

    public MetaFileDataRegistry getRegistry() {
        return registry;
    }

    public MetaFileNameParser getNameParser() {
        return nameParser;
    }

    private FileName parseName(final String name) {
        assert name != null;

        try {
            return nameParser.parseUri(name);
        }
        catch (FileSystemException e) {
            throw new RuntimeException("Failed to parse file name: " + name, e);
        }
    }

    public MetaFileData add(final FileName name, final MetaFileData data) {
        assert name != null;
        assert data != null;

        if (registry.containsData(name)) {
            log.warn("Replacing contents for name: {}", name);
        }

        registry.registerData(name, data);

        return data;
    }

    public MetaFileData add(final String name, final MetaFileData data) {
        return add(parseName(name), data);
    }

    public MetaFileData addFile(final FileName name) {
        return add(name, new MetaFileData(name, FileType.FILE));
    }

    public MetaFileData addFile(final String name) {
        return addFile(parseName(name));
    }

    public MetaFileData addFolder(final FileName name) {
        return add(name, new MetaFileData(name, FileType.FOLDER));
    }

    public MetaFileData addFolder(final String name) {
        return addFolder(parseName(name));
    }

    //
    // TODO: Add remove methods, and nested namespace muck, once we get base stuff working in the parser
    //
}