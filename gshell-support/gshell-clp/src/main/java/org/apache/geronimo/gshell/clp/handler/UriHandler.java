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

package org.apache.geronimo.gshell.clp.handler;

import org.apache.geronimo.gshell.clp.Descriptor;
import org.apache.geronimo.gshell.clp.ProcessingException;
import org.apache.geronimo.gshell.clp.setter.Setter;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Handler for file types.
 *
 * @version $Rev$ $Date$
 */
public class UriHandler
    extends Handler<URI>
{
    public UriHandler(final Descriptor desc, final Setter<? super URI> setter) {
        super(desc, setter);
    }

    @Override
    public int handle(final Parameters params) throws ProcessingException {
        assert params != null;

        String token = params.get(0);

        try {
            setter.set(new URI(token));
        }
        catch (URISyntaxException e) {
            throw new ProcessingException(e);
        }

        return 1;
    }

    @Override
    public String getDefaultToken() {
        return "URI";
    }
}