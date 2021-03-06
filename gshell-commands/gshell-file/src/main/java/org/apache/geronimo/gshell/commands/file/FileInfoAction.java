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

package org.apache.geronimo.gshell.commands.file;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileContentInfo;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.vfs.FileObjects;
import org.apache.geronimo.gshell.vfs.support.VfsActionSupport;

import java.security.cert.Certificate;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Display information about a file.
 *
 * @version $Rev$ $Date$
 */
public class FileInfoAction
    extends VfsActionSupport
{
    @Argument(required=true)
    private String path;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        FileObject file = resolveFile(context, path);

        io.info("URL: {}", file.getURL());
        io.info("Name: {}", file.getName());
        io.info("BaseName: {}", file.getName().getBaseName());
        io.info("Extension: {}", file.getName().getExtension());
        io.info("Path: {}", file.getName().getPath());
        io.info("Scheme: {}", file.getName().getScheme());
        io.info("URI: {}", file.getName().getURI());
        io.info("Root URI: {}", file.getName().getRootURI());
        io.info("Parent: {}", file.getName().getParent());
        io.info("Type: {}", file.getType());
        io.info("Exists: {}", file.exists());
        io.info("Readable: {}", file.isReadable());
        io.info("Writeable: {}", file.isWriteable());
        io.info("Root path: {}", file.getFileSystem().getRoot().getName().getPath());

        if (file.exists()) {
            FileContent content = file.getContent();
            FileContentInfo contentInfo = content.getContentInfo();
            io.info("Content type: {}", contentInfo.getContentType());
            io.info("Content encoding: {}", contentInfo.getContentEncoding());

            try {
                // noinspection unchecked
                Map<String,Object> attrs = content.getAttributes();
                if (attrs != null && !attrs.isEmpty()) {
                    io.info("Attributes:");
                    for (Map.Entry<String,Object> entry : attrs.entrySet()) {
                        io.info("    {}='{}'", entry.getKey(), entry.getValue());
                    }
                }
            }
            catch (FileSystemException e) {
                io.info("File attributes are NOT supported");
            }

            try {
                Certificate[] certs = content.getCertificates();
                if (certs != null && certs.length != 0) {
                    io.info("Certificate:");
                    for (Certificate cert : certs) {
                        io.info("    {}", cert);
                    }
                }
            }
            catch (FileSystemException e) {
                io.info("File certificates are NOT supported");
            }

            if (file.getType().equals(FileType.FILE)) {
                io.info("Size: {} bytes", content.getSize());
            }
            else if (file.getType().hasChildren() && file.isReadable()) {
                FileObject[] children = file.getChildren();
                io.info("Directory with {} files", children.length);

                for (int iterChildren = 0; iterChildren < children.length; iterChildren++) {
                    io.info("#{}:{}", iterChildren, children[iterChildren].getName());
                    if (iterChildren > 5) {
                        break;
                    }
                }
            }

            io.info("Last modified: {}", DateFormat.getInstance().format(new Date(content.getLastModifiedTime())));
        }
        else {
            io.info("The file does not exist");
        }

        FileObjects.close(file);
        
        return Result.SUCCESS;
    }
}
