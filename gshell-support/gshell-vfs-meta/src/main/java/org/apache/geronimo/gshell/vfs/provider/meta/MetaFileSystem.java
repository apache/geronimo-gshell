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
    private final MetaDataRegistry registry;

    public MetaFileSystem(final MetaDataRegistry registry, final FileName rootName, final FileSystemOptions options) {
        super(rootName, null, options);

        assert registry != null;
        this.registry = registry;
    }

    protected FileObject createFile(final FileName fileName) throws Exception {
        return new MetaFileObject(fileName, this);
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
        FileName name = file.getName();

        if (!registry.containsData(name)) {
            return new MetaData(name, FileType.IMAGINARY);
        }

        return registry.lookupData(name);
    }

    String[] listChildren(final FileName name) throws FileSystemException {
        assert name != null;

        MetaData data = registry.lookupData(name);
        Collection<MetaData> children = data.getChildren();

        List<String> names = new ArrayList<String>(children.size());
        for (MetaData child : children) {
            names.add(child.getName().getBaseName());
        }

        return names.toArray(new String[names.size()]);
    }
}