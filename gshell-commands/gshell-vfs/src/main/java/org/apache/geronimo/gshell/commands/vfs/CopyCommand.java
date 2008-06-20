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

package org.apache.geronimo.gshell.commands.vfs;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileUtil;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.command.CommandContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copy files.
 *
 * @version $Rev$ $Date$
 */
@CommandComponent(id="gshell-vfs:copy", description="Copy files between VFS sources")
public class CopyCommand
    extends VFSCommandSupport
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Argument(index=0, required=true, description="Source")
    private String sourceName;

    @Argument(index=1, required=true, description="Target")
    private String targetName;
public Object execute(final CommandContext context) throws Exception {
        assert context != null;
    
        FileSystemManager fsm = getFileSystemManager();
        FileObject source = fsm.resolveFile(sourceName);
        FileObject target = fsm.resolveFile(targetName);

        log.info("Copying {} -> {}", source, target);
        
        FileUtil.copyContent(source, target);

        return Result.SUCCESS;
    }
}