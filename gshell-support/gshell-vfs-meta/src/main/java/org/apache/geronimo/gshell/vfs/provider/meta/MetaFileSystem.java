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

    private MetaFileDataRegistry registry;

    public MetaFileSystem(final MetaFileDataRegistry registry, final FileName rootName, final FileSystemOptions options) {
        super(rootName, null, options);

        assert registry != null;
        this.registry = registry;

        //
        // TODO: Probably don't need this, as the root file should have been registered, but lets see
        //

        // Setup the root file's data
        MetaFileData data = new MetaFileData(rootName, FileType.FOLDER);
        data.updateLastModified();
        registry.register(rootName, data);
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

    @Override
    public void close() {
        super.close();
    }

    //
    // Internal bits invoked from MetaFileObject
    //

    //
    // TODO: Need to remove some of this, as the files aren't created per-normal, they need to be bound in the registry
    //
    
    void save(final MetaFileObject file) throws FileSystemException {
        assert file != null;

        log.debug("Saving: {}", file);

        FileName name = file.getName();
        MetaFileData data = file.getData();

        if (name.getDepth() > 0) {
            MetaFileData parentData = registry.lookup(file.getParent().getName());

            if (!parentData.hasChild(data)) {
                MetaFileObject parent = (MetaFileObject)file.getParent();
                parent.getData().addChild(data);
                parent.close();
            }
        }

        registry.register(name, data);
        file.getData().updateLastModified();
        file.close();
    }

    void attach(final MetaFileObject file) throws FileSystemException {
        assert file != null;

        log.debug("Attaching: {}", file);

        FileName name = file.getName();
        assert name != null;

        MetaFileData data = registry.lookup(name);
        if (data == null) {
            data = new MetaFileData(name);
        }

        file.setData(data);
    }

    void delete(final MetaFileObject file) throws FileSystemException {
        assert file != null;

        log.debug("Deleting: {}", file);

        if (file.getParent() == null) {
            throw new FileSystemException("Can not delete file-system root");
        }

        registry.remove(file.getName());

        MetaFileObject parent = (MetaFileObject) resolveFile(file.getParent().getName());
        parent.getData().removeChild(file.getData());
        parent.close();

        file.close();
    }

    String[] listChildren(final FileName name) throws FileSystemException {
        assert name != null;

        log.debug("Listing children: {}", name);

        MetaFileData data = registry.lookup(name);
        Collection<MetaFileData> children = data.getChildren();

        List<String> names = new ArrayList<String>(children.size());
        for (MetaFileData child : children) {
            names.add(child.getName().getBaseName());
        }

        return names.toArray(new String[names.size()]);
    }
}