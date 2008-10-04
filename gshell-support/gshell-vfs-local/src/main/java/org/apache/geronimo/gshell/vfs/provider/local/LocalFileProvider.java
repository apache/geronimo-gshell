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

package org.apache.geronimo.gshell.vfs.provider.local;

import org.apache.commons.vfs.provider.local.LocalFileName;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;

/**
 * Custom VFS local file provider.
 *
 * @version $Rev$ $Date$
 */
public class LocalFileProvider
    extends org.apache.commons.vfs.provider.local.DefaultLocalFileProvider
{
    protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions options) throws FileSystemException {
        LocalFileName rootName = (LocalFileName)name;
        return new LocalFileSystem(rootName, rootName.getRootFile(), options);
    }
}
