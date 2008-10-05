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
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.geronimo.gshell.vfs.provider.meta.data.MetaData;
import org.apache.geronimo.gshell.vfs.provider.meta.data.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Meta file system.
 *
 * @version $Rev$ $Date$
 */
public class MetaFileSystem
    extends AbstractFileSystem
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final MetaDataRegistry registry;

    public MetaFileSystem(final MetaDataRegistry registry, final FileName rootName, final FileSystemOptions options) {
        super(rootName, null, options);

        assert registry != null;
        this.registry = registry;
    }

    protected FileObject createFile(final FileName fileName) throws Exception {
        MetaFileObject file = new MetaFileObject(fileName, this);

        log.debug("Created file: {}", file);

        return file;
    }

    protected void addCapabilities(final Collection capabilities) {
        assert capabilities != null;

        // noinspection unchecked
        capabilities.addAll(MetaFileProvider.CAPABILITIES);
    }

    //
    // Internal bits invoked from MetaFileObject
    //

    MetaData lookupData(final MetaFileObject file) throws FileSystemException {
        assert file != null;

        log.debug("Looking up data: {}", file);

        FileName name = file.getName();
        assert name != null;

        // FIXME: This should probably toss an exception if the data is not registered
        MetaData data = registry.lookupData(name);
        if (data == null) {
            data = new MetaData(name, FileType.IMAGINARY);
        }

        return data;
    }

    String[] listChildren(final FileName name) throws FileSystemException {
        assert name != null;

        log.debug("Listing children: {}", name);

        MetaData data = registry.lookupData(name);
        Collection<MetaData> children = data.getChildren();

        List<String> names = new ArrayList<String>(children.size());
        for (MetaData child : children) {
            names.add(child.getName().getBaseName());
        }

        return names.toArray(new String[names.size()]);
    }
}