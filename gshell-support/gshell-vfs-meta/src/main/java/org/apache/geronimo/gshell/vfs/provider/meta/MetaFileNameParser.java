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
import org.apache.commons.vfs.provider.AbstractFileNameParser;
import org.apache.commons.vfs.provider.UriParser;
import org.apache.commons.vfs.provider.VfsComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Meta file name parser.
 *
 * @version $Rev$ $Date$
 */
public class MetaFileNameParser
    extends AbstractFileNameParser
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String defaultScheme;

    public MetaFileNameParser(final String scheme) {
        assert scheme != null;

        this.defaultScheme = scheme;
    }

    public MetaFileNameParser() {
        this(MetaFileName.SCHEME);
    }

    public FileName parseUri(final FileName baseName, final String fileName) throws FileSystemException {
        // baseName could be null
        assert fileName != null;

        log.trace("Parsing URI; base={}, filename={}", baseName, fileName);

        /*
        if (baseName != null) {
            //
            // FIXME: Do something with base.  Maybe need to build a filename first, then if its relative, prefix base?
            //        only support using base when its meta: ?
            //

            throw new UnsupportedOperationException("Base prefixing is not yet supported");
        }
        */
        
        StringBuffer buff = new StringBuffer();

        String scheme = UriParser.extractScheme(fileName, buff);
        if (scheme == null) {
            scheme = defaultScheme;
        }

        UriParser.canonicalizePath(buff, 0, buff.length(), this);
        UriParser.fixSeparators(buff);
        FileType type = UriParser.normalisePath(buff);

        if (log.isTraceEnabled()) {
            log.trace("Creating file name; scheme={}, path={}, type={}", new Object[] {scheme, buff, type});
        }

        //
        // TODO: Need to make sure that we end up with something looking abs here?
        //
        
        FileName name = new MetaFileName(scheme, buff.toString(), type);

        log.trace("Created file name: {}", name);

        return name;
    }

    public FileName parseUri(final VfsComponentContext context, final FileName baseName, final String fileName) throws FileSystemException {
        return parseUri(baseName, fileName);
    }

    public FileName parseUri(final String fileName) throws FileSystemException {
        return parseUri(null, fileName);
    }
}