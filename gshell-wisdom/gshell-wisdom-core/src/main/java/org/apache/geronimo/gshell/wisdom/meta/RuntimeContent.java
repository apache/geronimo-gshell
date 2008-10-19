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

import org.apache.geronimo.gshell.vfs.provider.meta.data.MetaDataContent;
import org.apache.geronimo.gshell.vfs.provider.meta.data.support.MapMetaDataContentSupport;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link MetaDataContent} to return details about {@link Runtime}.
 *
 * @version $Rev$ $Date$
 */
public class RuntimeContent
    extends MapMetaDataContentSupport<String,Object>
{
    protected Map<String, Object> getMap() {
        Map<String,Object> map = new LinkedHashMap<String,Object>();

        Runtime rt = Runtime.getRuntime();
        map.put("freeMemory", rt.freeMemory());
        map.put("maxMemory", rt.maxMemory());
        map.put("totalMemory", rt.totalMemory());
        map.put("availableProcessors", rt.availableProcessors());
        
        return map;
    }
}