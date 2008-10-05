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

package org.apache.geronimo.gshell.wisdom.meta;

import org.apache.geronimo.gshell.vfs.provider.meta.MetaData;
import org.apache.geronimo.gshell.vfs.provider.meta.MetaDataRegistry;
import org.apache.geronimo.gshell.vfs.provider.meta.MetaDataRegistryConfigurer;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Installs {@link MetaData} into the {@link MetaDataRegistry}.
 *
 * @version $Rev$ $Date$
 */
public class MetaDataInstaller
{
    @Autowired
    private MetaDataRegistry metaRegistry;

    // private MetaDataRegistryConfigurer metaConfig;

    @PostConstruct
    public void init() throws Exception {
        assert metaRegistry != null;
        MetaDataRegistryConfigurer metaConfig = new MetaDataRegistryConfigurer(metaRegistry);

        // HACK: Hard code this for now, evetually configure via spring
        metaConfig.addFolder("/system");
        FileName name = metaConfig.getNameParser().parseUri("/system/properties");
        metaConfig.add(name, new SystemPropertiesMetaData(name));
    }
}