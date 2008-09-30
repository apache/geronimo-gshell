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
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
    private static final String PARENT_TOKEN = "..";

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private FileSystemAccess fileSystemAccess;

    public int complete(final String buffer, final int cursor, final List candidates) {
        // buffer may be null
        assert candidates != null;

        String path = buffer == null ? "" : buffer;

        log.trace("Path: '{}'", path);
        
        try {
            assert fileSystemAccess != null;
            FileObject file = fileSystemAccess.resolveFile(path);

            log.trace("Resolved file: {}", file);

            final String search;

            if (!file.exists() || file.getType() == FileType.FILE) {
                search = file.getName().getBaseName();
                
                if (!path.endsWith(FileName.SEPARATOR)) {
                    file = file.getParent();
                }
            }
            else if (file.getType() == FileType.FOLDER && !path.endsWith(FileName.SEPARATOR)) {
                // Handle the special cases when we resolved to a directory, with out a trailing seperator,
                // complete to the directory + "/" first.

                StringBuilder buff = new StringBuilder();

                // Another special case here with "..", don't use the file name, just use the ".."
                if (path.endsWith(PARENT_TOKEN)) {
                    buff.append(PARENT_TOKEN);
                }
                else {
                    buff.append(file.getName().getBaseName());
                }

                buff.append(FileName.SEPARATOR);

                //
                // TODO: Need to encode spaces, once the parser can handle escaped spaces.
                //
                
                // noinspection unchecked
                candidates.add(buff.toString());

                int result = path.lastIndexOf(FileName.SEPARATOR) + 1;

                log.trace("Result: {}", result);
                
                return result;
            }
            else {
                // Else we want to show all contents
                search = null;
            }

            log.trace("Base File: {}", file);
            log.trace("Search: {}", search);

            if (file != null) {
                FileObject[] files;

                if (search != null) {
                    FileFilter filter = new FileFilter() {
                        public boolean accept(final FileSelectInfo selection) {
                            assert selection != null;

                            if (log.isTraceEnabled()) {
                                log.trace("Filtering selection: {}", selection.getFile().getName());
                            }

                            return selection.getFile().getName().getBaseName().startsWith(search);
                        }
                    };

                    files = file.findFiles(new FileFilterSelector(filter));
                }
                else {
                    files = file.getChildren();
                }

                if (files == null || files.length == 0) {
                    log.trace("No matching files found");
                }
                else {
                    log.trace("Found {} matching files:", files.length);

                    for (FileObject child : files) {
                        log.trace("    {}", child);

                        StringBuilder buff = new StringBuilder();
                        buff.append(child.getName().getBaseName());

                        //
                        // TODO: Need to encode spaces, once the parser can handle escaped spaces.
                        //

                        if (files.length == 1 && child.getType() == FileType.FOLDER) {
                            buff.append(FileName.SEPARATOR);
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

            int result;

            if (search == null) {
                result  = path.length();
            }
            else {
                result = path.lastIndexOf(FileName.SEPARATOR) + 1;
            }

            log.trace("Result: {}", result);

            return result;
        }
        catch (FileSystemException e) {
            log.trace("Unable to complete path: " + path, e);
            return -1;
        }
    }
}