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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link MetaFileDataRegistry} component.
 *
 * @version $Rev$ $Date$
 */
public class MetaFileDataRegistryImpl
    implements MetaFileDataRegistry
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Map<FileName,MetaFileData> nodes = Collections.synchronizedMap(new HashMap<FileName,MetaFileData>());

    //
    // TODO: Add some helpers to assist registration of new data.
    //

    public void register(final FileName name, final MetaFileData data) {
        assert name != null;
        assert data != null;

        log.debug("Registering: {} -> {}", name, data);

        nodes.put(name, data);
    }

    public void remove(final FileName name) {
        assert name != null;

        log.debug("Removing: {}", name);

        nodes.remove(name);
    }

    public MetaFileData lookup(final FileName name) {
        assert name != null;

        return nodes.get(name);
    }
}