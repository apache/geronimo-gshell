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
import org.apache.commons.vfs.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Meta data.
 *
 * @version $Rev$ $Date$
 */
public class MetaData
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final FileName name;
    
    private final FileType type;

    private final MetaDataContent content;

    private final Map<String,Object> attributes = new HashMap<String,Object>();

    private final Collection<MetaData> children = new ArrayList<MetaData>();

    private long lastModified = -1;

    //
    // TODO: Consider changing "name" to a String here?  name.getParent() is used in a few places, so have to resolve that first, perhaps expose MetaData getParent() ?
    //
    
    public MetaData(final FileName name, final FileType type, final MetaDataContent content) {
        assert name != null;
        assert type != null;
        // content may be null

        this.name = name;
        this.type = type;
        this.content = content;
    }

    public MetaData(final FileName name, final FileType type) {
        this(name, type, null);
    }

    public MetaData(final FileName name, final MetaDataContent content) {
        this(name, FileType.FILE_OR_FOLDER, content);
    }

    public MetaData(final FileName name) {
        this(name, FileType.FILE_OR_FOLDER, null);
    }

    public FileName getName() {
        return name;
    }

    public FileType getType() {
        return type;
    }

    public byte[] getBuffer() {
        return content != null ? content.getBuffer() : null;
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

    public Collection<MetaData> getChildren() {
        return children;
    }

    public void addChild(final MetaData data) {
        assert data != null;

        if (!getType().hasChildren()) {
            throw new MetaDataException("A child can only be added in a folder");
        }
        if (hasChild(data)) {
            throw new MetaDataException("Child already exists: " + data);
        }

        log.trace("Adding child: {}", data);

        getChildren().add(data);
        updateLastModified();
    }

    public void removeChild(final MetaData data) {
        assert data != null;

        if (!getType().hasChildren()) {
            throw new MetaDataException("A child can only be removed from a folder");
        }
        if (!hasChild(data)) {
            throw new MetaDataException("Child not found: " + data);
        }

        log.trace("Removing child: {}", data);
        
        getChildren().remove(data);
        updateLastModified();
    }

    public boolean hasChild(final MetaData data) {
        assert data != null;

        return getChildren().contains(data);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        MetaData that = (MetaData) obj;
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