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

package org.apache.geronimo.gshell.artifact.ivy;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.util.filter.Filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * AND artifact filter.
 *
 * @version $Rev$ $Date$
 */
public class AndArtifactFilter
    implements Filter
{
    private final List<Filter> filters = new ArrayList<Filter>();

    public boolean accept(final Object obj) {
        if (obj instanceof Artifact) {
            return include((Artifact)obj);
        }
        return false;
    }

    public boolean include(final Artifact artifact) {
        boolean include = true;
        for (Iterator<Filter> i = filters.iterator(); i.hasNext() && include;) {
            Filter filter = i.next();
            if (!filter.accept(artifact)) {
                include = false;
            }
        }
        return include;
    }

    public void add(final Filter filter) {
        filters.add(filter);
    }
}