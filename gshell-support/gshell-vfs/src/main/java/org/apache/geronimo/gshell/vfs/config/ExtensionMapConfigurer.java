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

import javax.annotation.PostConstruct;

/**
 * Configures an extension mapping.
 *
 * @version $Rev$ $Date$
 */
public class ExtensionMapConfigurer
    extends FileSystemManagerConfigurerSupport
{
    private String extension;

    private String scheme;

    public void setExtension(final String extension) {
        this.extension = extension;
    }

    public void setScheme(final String scheme) {
        this.scheme = scheme;
    }

    @PostConstruct
    public void init() {
        if (extension == null) {
            throw new RuntimeException("Missing property: extension");
        }
        if (scheme == null) {
            throw new RuntimeException("Missing property: scheme");
        }

        log.debug("Adding extension mapping: {} -> {}", extension, scheme);
        ConfigurableFileSystemManager fsm = getFileSystemManager();
        fsm.addExtensionMap(extension, scheme);
    }
}