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

package org.apache.geronimo.gshell.model.common;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.geronimo.gshell.model.Element;

/**
 * Remote repository configuration.
 *
 * @version $Rev$ $Date$
 */
@XStreamAlias("remoteRepository")
public class RemoteRepository
    extends Element
{
    private String id;

    private String location;

    public String getId() {
        // If no id was configured, and we have a valid location, then try to figure out a reasonable ID from the URI
        if (id == null && location != null) {
            try {
                // FIXME: This dosen't give too well with URI's that don't have a <host> bit, like file:
                return getLocationUri().getHost();
            }
            catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public URI getLocationUri() throws URISyntaxException {
        String tmp = getLocation();

        if (tmp != null) {
            return new URI(tmp);
        }

        return null;
    }

    public void setLocationUri(final URI uri) {
        assert uri != null;

        setLocation(uri.toString());
    }
}