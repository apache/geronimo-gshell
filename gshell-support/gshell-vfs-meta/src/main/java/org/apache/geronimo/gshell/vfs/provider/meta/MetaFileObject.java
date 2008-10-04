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
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;

/**
 * Meta file object.
 *
 * @version $Rev$ $Date$
 */
public class MetaFileObject
    extends AbstractFileObject
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final MetaFileSystem fileSystem;

    private MetaFileData data;

    public MetaFileObject(final FileName fileName, final MetaFileSystem fileSystem) {
        super(fileName, fileSystem);

        // Save for uncasted typed access
        this.fileSystem = fileSystem;
    }

    MetaFileData getData() {
        assert data != null;
        
        return data;
    }

    void setData(final MetaFileData data) {
        assert data != null;
        
        this.data = data;
    }


    @Override
    protected FileType doGetType() throws Exception {
        return getData().getType();
    }

    @Override
    protected long doGetLastModifiedTime() throws Exception {
        return getData().getLastModified();
    }

    @Override
    protected long doGetContentSize() throws Exception {
        // Meta file is always empty
        return 0;
    }

    @Override
    protected InputStream doGetInputStream() throws Exception {
        // No contents thus no input stream
        return null;
    }

    @Override
    protected Map<String,Object> doGetAttributes() {
        return getData().getAttributes();
    }

    @Override
    protected void doSetAttribute(final String name, final Object value) {
        getData().getAttributes().put(name, value);
    }

    protected void doRemoveAttribute(final String name) {
        getData().getAttributes().remove(name);
    }

    @Override
    protected String[] doListChildren() throws Exception {
        return fileSystem.listChildren(getName());
    }

    @Override
    protected void doDelete() throws Exception {
        fileSystem.delete(this);
    }

    @Override
    protected void doCreateFolder() throws Exception {
        injectType(FileType.FOLDER);
        fileSystem.save(this);
    }

    @Override
    protected void doAttach() throws Exception {
        fileSystem.attach(this);
    }

    @Override
    protected void doDetach() throws Exception {
        data = null;
    }

    @Override
    protected void injectType(final FileType type) {
        assert type != null;

        getData().setType(type);
        super.injectType(type);
    }
}