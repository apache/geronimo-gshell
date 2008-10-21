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

package org.apache.geronimo.gshell.xstore;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.geronimo.gshell.io.Closer;
import org.apache.geronimo.gshell.marshal.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@link XStoreRecord} implementation.
 *
 * @version $Rev$ $Date$
 */
public class XStoreRecordImpl
    implements XStoreRecord
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final XStoreImpl xstore;

    private final FileObject file;

    XStoreRecordImpl(final XStoreImpl xstore, final FileObject file) {
        assert xstore != null;
        this.xstore = xstore;
        assert file != null;
        this.file = file;
    }
    
    public String getPath() {
        return file.getName().getPath();
    }

    public String toString() {
        return getPath();    
    }

    public boolean exists() {
        try {
            return file.exists();
        }
        catch (FileSystemException e) {
            throw new XStoreException(e);
        }
    }

    // TODO: Add marshaller cache
    
    public void set(final Object value) {
        assert value != null;

        log.trace("Setting: {} -> {}", file.getName(), value);

        BufferedOutputStream output = null;
        try {
            Marshaller marshaller = xstore.getMarshaller(value.getClass());
            output = new BufferedOutputStream(file.getContent().getOutputStream());
            // noinspection unchecked
            marshaller.marshal(value, output);
        }
        catch (FileSystemException e) {
            throw new XStoreException(e);
        }
        finally {
            Closer.close(output);
        }
    }

    public <T> T get(final Class<T> type) {
        assert type != null;

        log.trace("Getting: {}", file);

        BufferedInputStream input = null;

        try {
            Marshaller<T> marshaller = xstore.getMarshaller(type);

            FileObject parent = file.getParent();
            if (parent != null && !parent.exists()) {
                parent.createFolder();
            }

            input = new BufferedInputStream(file.getContent().getInputStream());
            T value = marshaller.unmarshal(input);
            log.trace("Value: {}", value);
            return value;
        }
        catch (FileSystemException e) {
            throw new XStoreException(e);
        }
        finally {
            Closer.close(input);
        }
    }

    public void close() {
        log.trace("Closing: {}", file);
        
        try {
            file.close();
        }
        catch (FileSystemException e) {
            throw new XStoreException(e);
        }
    }

    public boolean delete() {
        log.trace("Deleting: {}", file);

        try {
            return file.delete();
        }
        catch (FileSystemException e) {
            throw new XStoreException(e);
        }
    }

    public void refresh() {
        log.trace("Refreshing: {}", file);

        try {
            file.refresh();
        }
        catch (FileSystemException e) {
            throw new XStoreException(e);
        }
    }

    public XStoreRecord getParent() {
        FileObject parentFile;

        try {
            parentFile = file.getParent();
        }
        catch (FileSystemException e) {
            throw new XStoreException(e);
        }

        if (parentFile != null) {
            return new XStoreRecordImpl(xstore, parentFile);
        }
        return null;
    }

    public Collection<XStoreRecord> getChilden() {
        FileObject[] files;

        try {
            files = file.getChildren();
        }
        catch (FileSystemException e) {
            throw new XStoreException(e);
        }

        List<XStoreRecord> children = new ArrayList<XStoreRecord>(files.length);

        for (FileObject file : files) {
            children.add(new XStoreRecordImpl(xstore, file));
        }

        return children;
    }

    public XStorePointer createPointer() {
        return xstore.createPointer(getPath());
    }
}