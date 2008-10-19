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

package org.apache.geronimo.gshell.vfs.config;

import org.apache.commons.vfs.FileSystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

/**
 * Support for configuration of a {@link FileSystemManager}.
 *
 * @version $Rev$ $Date$
 */
public class FileSystemManagerConfigurer
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ConfigurableFileSystemManager fileSystemManager;

    // TODO: Maybe add a provider configurer, which will support mapping extentions, mime-tyeps for a single provider?

    // providers Map<List<String>,Provider>

    // operation providers Map<List<String>,Provider>

    // extentions

    // mime-types

    public FileSystemManagerConfigurer(final ConfigurableFileSystemManager fileSystemManager) {
        assert fileSystemManager != null;
        this.fileSystemManager = fileSystemManager;
    }

    @PostConstruct
    public void init() {
        assert fileSystemManager != null;
        log.debug("Configuring file system manager: {}", fileSystemManager);
    }
}