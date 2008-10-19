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

import org.apache.commons.vfs.CacheStrategy;
import org.apache.commons.vfs.FileContentInfoFactory;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FilesCache;
import org.apache.commons.vfs.cache.SoftRefFilesCache;
import org.apache.commons.vfs.impl.DefaultFileReplicator;
import org.apache.commons.vfs.impl.FileContentInfoFilenameFactory;
import org.apache.commons.vfs.impl.PrivilegedFileReplicator;
import org.apache.commons.vfs.provider.FileProvider;
import org.apache.commons.vfs.provider.FileReplicator;
import org.apache.commons.vfs.provider.TemporaryFileStore;
import org.apache.commons.vfs.provider.url.UrlFileProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import javax.annotation.PostConstruct;

/**
 * Spring {@link FactoryBean} to construct a {@link FileSystemManager} instance.
 *
 * @version $Rev$ $Date$
 */
public class FileSystemManagerFactoryBean
    implements FactoryBean
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private FilesCache filesCache;

    private CacheStrategy cacheStrategy = CacheStrategy.ON_RESOLVE;

    private FileReplicator fileReplicator;

    private TemporaryFileStore temporaryFileStore;

    private FileContentInfoFactory fileContentInfoFactory;

    private FileProvider defaultProvider;

    // FileObjectDecorator (Class/Constructor of DecoratedFileObject? or make a factory?)

    public void setFilesCache(final FilesCache cache) {
        this.filesCache = cache;
    }

    public void setCacheStrategy(final CacheStrategy strategy) {
        this.cacheStrategy = strategy;
    }

    public void setFileReplicator(final FileReplicator replicator) {
        this.fileReplicator = replicator;
    }

    public void setTemporaryFileStore(final TemporaryFileStore store) {
        this.temporaryFileStore = store;
    }

    public void setFileContentInfoFactory(final FileContentInfoFactory factory) {
        this.fileContentInfoFactory = factory;
    }

    public void setDefaultProvider(final FileProvider provider) {
        this.defaultProvider = provider;
    }

    @PostConstruct
    public void init() {
        if (filesCache == null) {
            filesCache = new SoftRefFilesCache();
        }

        if (fileReplicator == null || temporaryFileStore == null) {
            DefaultFileReplicator replicator = new DefaultFileReplicator();
            if (fileReplicator == null) {
                fileReplicator = new PrivilegedFileReplicator(replicator);
            }
            if (temporaryFileStore == null) {
                temporaryFileStore = replicator;
            }
        }

        if (fileContentInfoFactory == null) {
            fileContentInfoFactory = new FileContentInfoFilenameFactory();
        }

        if (defaultProvider == null) {
            defaultProvider = new UrlFileProvider();
        }
    }

    //
    // FactoryBean
    //

    public Object getObject() throws Exception {
        ConfigurableFileSystemManager fsm = new ConfigurableFileSystemManager();

        assert fileReplicator != null;
        log.debug("File replicator: {}", fileReplicator);
        fsm.setReplicator(fileReplicator);

        assert temporaryFileStore != null;
        log.debug("Temporary file store: {}", temporaryFileStore);
        fsm.setTemporaryFileStore(temporaryFileStore);

        assert filesCache != null;
        log.debug("Files cache: {}", filesCache);
        fsm.setFilesCache(filesCache);

        assert cacheStrategy != null;
        log.debug("Cache strategy: {}", cacheStrategy);
        fsm.setCacheStrategy(cacheStrategy);

        assert fileContentInfoFactory != null;
        log.debug("File content info factory: {}", fileContentInfoFactory);
        fsm.setFileContentInfoFactory(fileContentInfoFactory);

        assert defaultProvider != null;
        log.debug("Default provider: {}", defaultProvider);
        fsm.setDefaultProvider(defaultProvider);

        // Finally init the manager
        fsm.init();
        
        return fsm;
    }

    public Class getObjectType() {
        return ConfigurableFileSystemManager.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
