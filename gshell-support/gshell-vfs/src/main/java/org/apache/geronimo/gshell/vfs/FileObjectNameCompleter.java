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

package org.apache.geronimo.gshell.vfs;

import jline.Completor;
import org.apache.commons.vfs.FileFilter;
import org.apache.commons.vfs.FileFilterSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * VFS {@link FileObject} name completer.
 *
 * @version $Rev$ $Date$
 */
public class FileObjectNameCompleter
    implements Completor
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private FileSystemAccess fileSystemAccess;

    public int complete(final String buffer, final int cursor, final List candidates) {
        // buffer may be null
        assert candidates != null;

        String path = buffer == null ? "" : buffer;

        log.debug("Path: '{}'", path);
        
        try {
            assert fileSystemAccess != null;
            FileObject dir = fileSystemAccess.resolveFile(path);

            //
            // FIXME: The processing logic here is not correct...
            //
            
            /*
            log.debug("Dir (before): {}", dir);
            if (!path.endsWith(File.separator)) {
                dir = dir.getParent();
            }
            log.debug("Dir (after): {}", dir);
            */
            log.debug("Dir: {}", dir);

            if (dir != null) {
                final String prefix = path;
                FileFilter filter = new FileFilter() {
                    public boolean accept(final FileSelectInfo selection) {
                        assert selection != null;

                        if (log.isDebugEnabled()) {
                            log.debug("Filtering selection: {}", selection.getFile().getName());
                        }

                        return selection.getFile().getName().getPath().startsWith(prefix);
                    }
                };

                FileObject[] files = dir.findFiles(new FileFilterSelector(filter));

                if (files == null || files.length == 0) {
                    log.debug("No matching files found");
                }
                else {
                    log.debug("Found {} matching files:", files.length);

                    for (FileObject file : files) {
                        log.debug("    {}", file);

                        StringBuilder buff = new StringBuilder();
                        buff.append(file.getName().getBaseName());

                        if (files.length == 1 && file.getType() == FileType.FOLDER) {
                            buff.append(File.separator);
                        }
                        else {
                            buff.append(" ");
                        }

                        // noinspection unchecked
                        candidates.add(buff.toString());
                    }

                    // noinspection unchecked
                    Collections.sort(candidates);
                }
            }

            return path.lastIndexOf(File.separator) + File.separator.length();
        }
        catch (FileSystemException e) {
            throw new RuntimeException("Unable to complete path: " + path, e);
        }
    }
}