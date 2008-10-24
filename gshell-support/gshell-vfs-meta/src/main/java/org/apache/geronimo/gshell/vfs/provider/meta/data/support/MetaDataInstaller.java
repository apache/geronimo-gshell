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

package org.apache.geronimo.gshell.vfs.provider.meta.data.support;

import org.apache.geronimo.gshell.vfs.provider.meta.MetaFileNameParser;
import org.apache.geronimo.gshell.vfs.provider.meta.data.MetaData;
import org.apache.geronimo.gshell.vfs.provider.meta.data.MetaDataContent;
import org.apache.geronimo.gshell.vfs.provider.meta.data.MetaDataRegistry;

import java.util.Map;

/**
 * Installs {@link MetaData} into the {@link MetaDataRegistry}.
 *
 * @version $Rev$ $Date$
 */
public class MetaDataInstaller
{
    private final MetaDataRegistry metaRegistry;

    private final MetaFileNameParser nameParser = new MetaFileNameParser();

    private Map<String,MetaDataContent> contentNodes;

    public MetaDataInstaller(final MetaDataRegistry metaRegistry) {
        assert metaRegistry != null;
        this.metaRegistry = metaRegistry;
    }

    public void setContentNodes(final Map<String, MetaDataContent> nodes) {
        this.contentNodes = nodes;
    }

    // @PostConstruct
    public void init() throws Exception {
        if (contentNodes != null && !contentNodes.isEmpty()) {
            for (Map.Entry<String,MetaDataContent> entry : contentNodes.entrySet()) {
                MetaData data = new MetaData(nameParser.parseUri(entry.getKey()), entry.getValue());
                metaRegistry.registerData(data.getName(), data);
            }
        }
    }
}