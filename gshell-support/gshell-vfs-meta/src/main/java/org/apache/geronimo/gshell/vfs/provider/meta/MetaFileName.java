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
import org.apache.commons.vfs.provider.AbstractFileName;

/**
 * Meta file name.
 *
 * @version $Rev$ $Date$
 */
public class MetaFileName
    extends AbstractFileName
{
    public static final String SCHEME = "meta";
    
    protected MetaFileName(final String scheme, final String path, final FileType type) {
        super(scheme, path, type);
    }

    public FileName createName(final String path, final FileType type) {
        return new MetaFileName(getScheme(), path, type);
    }

    protected void appendRootUri(final StringBuffer buffer, final boolean addPassword) {
        assert buffer != null;

        //
        // TODO: May want to always append ":/", and make sure the path has that stuff stripped off
        //

        buffer.append(getScheme()).append(":");
    }
}