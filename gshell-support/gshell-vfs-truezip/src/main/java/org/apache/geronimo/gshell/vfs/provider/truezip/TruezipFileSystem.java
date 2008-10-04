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

package org.apache.geronimo.gshell.vfs.provider.truezip;

import java.io.FilePermission;
import java.util.Collection;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import de.schlichtherle.io.ArchiveException;
import de.schlichtherle.io.File;

/**
 * <h href="https://truezip.dev.java.net">TrueZIP</a> file system.
 *
 * @version $Rev$ $Date$
 */
public class TruezipFileSystem
    extends AbstractFileSystem
    implements FileSystem
{
    public TruezipFileSystem(final FileName rootName, final String rootFile, final FileSystemOptions opts) {
        super(rootName, null, opts);
    }

    protected TruezipFileSystem(final FileName rootName,
        final FileObject file,
        final FileSystemOptions fileSystemOptions) throws FileSystemException {
        super(rootName, file, fileSystemOptions);
    }

    /**
     * Creates a file object.
     */
    protected FileObject createFile(final FileName name) throws FileSystemException {
        // Create the file
        return new TruezipFileObject(this, name);
    }

    /**
     * Returns the capabilities of this file system.
     */
    protected void addCapabilities(final Collection caps) {
        // noinspection unchecked
        caps.addAll(TruezipFileProvider.capabilities);
    }

    /**
     * Creates a temporary local copy of a file and its descendents.
     */
    protected java.io.File doReplicateFile(final FileObject fileObject, final FileSelector selector) throws Exception {
        final TruezipFileObject localFile = (TruezipFileObject) fileObject;
        final File file = localFile.getLocalFile();

        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            final FilePermission requiredPerm = new FilePermission(file.getAbsolutePath(), "read");
            sm.checkPermission(requiredPerm);
        }

        return file;
    }

    protected void doCloseCommunicationLink() {
        try {
            File.umount();
        }
        catch (ArchiveException e) {
            throw new RuntimeException(e);
        }
    }

}
