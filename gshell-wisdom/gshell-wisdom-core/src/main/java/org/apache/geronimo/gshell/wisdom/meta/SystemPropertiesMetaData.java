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
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * {@link MetaData} to return the contents of {@link System#getProperties}.
 *
 * @version $Rev$ $Date$
 */
public class SystemPropertiesMetaData
    extends MetaData
{
    public SystemPropertiesMetaData(final FileName name) {
        super(name, FileType.FILE);
    }

    @Override
    public byte[] getBuffer() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        Properties props = System.getProperties();

        try {
            props.store(output, "System Properties");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        return output.toByteArray();
    }
}