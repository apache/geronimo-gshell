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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Meta file data.
 *
 * @version $Rev$ $Date$
 */
public class MetaFileData
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final FileName name;

    //
    //  TODO: Consider making all meta files FileType.FILE_OR_FOLDER
    //
    
    private final FileType type;

    private long lastModified = -1;

    private final Map<String,Object> attributes = /*Collections.synchronizedMap(*/new HashMap<String,Object>()/*)*/;

    private final Collection<MetaFileData> children = /*Collections.synchronizedCollection(*/new ArrayList<MetaFileData>()/*)*/;

    public MetaFileData(final FileName name, final FileType type) {
        assert name != null;
        assert type != null;

        this.name = name;
        this.type = type;
    }

    public FileName getName() {
        return name;
    }

    public FileType getType() {
        return type;
    }

    public void updateLastModified() {
        lastModified = System.currentTimeMillis();
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public Map<String,Object> getAttributes() {
        return attributes;
    }

    public void addAttribute(final String name, final Object value) {
        assert name != null;
        // value could be null

        getAttributes().put(name, value);

        updateLastModified();
    }

    public Object removeAttribute(final String name) {
        assert name != null;

        Object old = getAttributes().remove(name);

        updateLastModified();

        return old;
    }

    public Collection<MetaFileData> getChildren() {
        return children;
    }

    public void addChild(final MetaFileData data) throws FileSystemException {
        assert data != null;

        if (!getType().hasChildren()) {
            throw new FileSystemException("A child can only be added in a folder");
        }
        if (hasChild(data)) {
            throw new FileSystemException("Child already exists: " + data);
        }

        log.debug("Adding child: {}", data);

        getChildren().add(data);
        updateLastModified();
    }

    public void removeChild(final MetaFileData data) throws FileSystemException{
        assert data != null;

        if (!getType().hasChildren()) {
            throw new FileSystemException("A child can only be removed from a folder");
        }
        if (!hasChild(data)) {
            throw new FileSystemException("Child not found: " + data);
        }

        log.debug("Removing child: {}", data);
        
        getChildren().remove(data);
        updateLastModified();
    }

    public boolean hasChild(final MetaFileData data) {
        assert data != null;

        return getChildren().contains(data);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        MetaFileData that = (MetaFileData) obj;
        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return getName().toString();
    }
}