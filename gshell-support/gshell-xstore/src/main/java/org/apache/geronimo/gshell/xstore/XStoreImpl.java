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

import org.apache.geronimo.gshell.vfs.FileSystemAccess;
import org.apache.geronimo.gshell.marshal.Marshaller;
import org.apache.geronimo.gshell.marshal.MarshallerSupport;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

/**
 * {@link XStore} component.
 *
 * @version $Rev$ $Date$
 */
public class XStoreImpl
    implements XStore
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final FileSystemAccess fileSystemAccess;

    private String rootUri;

    private FileSystem fileSystem;

    public XStoreImpl(final FileSystemAccess fileSystemAccess) {
        assert fileSystemAccess != null;
        this.fileSystemAccess = fileSystemAccess;
    }

    public String getRootUri() {
        if (rootUri == null) {
            throw new IllegalStateException("Missing property: rootUri");
        }
        return rootUri;
    }

    public void setRootUri(final String rootUri) {
        this.rootUri = rootUri;
    }

    private FileSystem getFileSystem() {
        if (fileSystem == null) {
            try {
                assert fileSystemAccess != null;
                String uri = getRootUri();

                FileObject root = fileSystemAccess.resolveFile(null, uri);
                log.debug("Root: {}", uri);

                FileObject file = fileSystemAccess.getManager().createVirtualFileSystem(root);
                fileSystem = file.getFileSystem();
                log.debug("File system: {}", fileSystem);
            }
            catch (FileSystemException e) {
                throw new XStoreException(e);
            }
        }

        return fileSystem;
    }

    public XStoreRecord resolveRecord(final String path) {
        assert path != null;

        final XStoreImpl prev = XStoreHolder.get();
        XStoreHolder.set(this);

        try {
            FileObject file = getFileSystem().resolveFile(path);
            return new XStoreRecordImpl(this, file);
        }
        catch (FileSystemException e) {
            throw new XStoreException(e);
        }
        finally {
            if (prev != null) {
                XStoreHolder.set(prev);
            }
        }
    }

    public XStorePointer createPointer(final String path) {
        assert path != null;

        return new XStorePointerImpl(this, path);
    }

    private final Map<Class,Marshaller> marshallers = new HashMap<Class,Marshaller>();

    @SuppressWarnings({"unchecked"})
    <T> Marshaller<T> getMarshaller(final Class<T> type) {
        assert type != null;

        Marshaller<T> marshaller = marshallers.get(type);
        if (marshaller == null) {
            marshaller = new MarshallerSupport<T>(type);
            marshallers.put(type, marshaller);
        }

        return marshaller;
    }
}