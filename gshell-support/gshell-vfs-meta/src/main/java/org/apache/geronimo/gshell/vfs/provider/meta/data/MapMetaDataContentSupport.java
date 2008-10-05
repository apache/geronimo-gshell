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

package org.apache.geronimo.gshell.vfs.provider.meta.data;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

/**
 * Support for {@link MetaDataContent} generated for a {@link Map}.
 *
 * @version $Rev$ $Date$
 */
public abstract class MapMetaDataContentSupport<K,V>
    implements MetaDataContent
{
    public byte[] getBuffer() {
        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);

        for (Map.Entry<K,V> entry : getMap().entrySet()) {
            out.print(entry.getKey());
            out.print("=");
            out.println(entry.getValue());
        }

        out.flush();
        out.close();

        return writer.toString().getBytes();
    }

    protected abstract Map<K,V> getMap();
}