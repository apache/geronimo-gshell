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

import org.apache.geronimo.gshell.event.Event;
import org.apache.commons.vfs.FileName;

/**
 * Event fired when some {@link MetaData} has been removed.
 *
 * @version $Rev$ $Date$
 */
public class MetaDataRemovedEvent
    implements Event
{
    private final FileName name;

    private final MetaData data;

    public MetaDataRemovedEvent(final FileName name, final MetaData data) {
        assert name != null;
        this.name = name;

        assert data != null;
        this.data = data;
    }

    public MetaData getData() {
        return data;
    }

    public FileName getName() {
        return name;
    }
}