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

package org.apache.geronimo.gshell.remote.message;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.mina.filter.reqres.Request;
import org.apache.mina.filter.reqres.ResponseInspector;
import org.apache.mina.filter.reqres.ResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for request/response {@link Message} passing.
 *
 * @version $Rev$ $Date$
 */
public class MessageResponseInspector
    implements ResponseInspector
{
    private Logger log = LoggerFactory.getLogger(getClass());

    private Set<UUID> registeredIds = new HashSet<UUID>();

    public synchronized void register(final Request req) {
        assert req != null;

        UUID id = (UUID) req.getId();

        if (registeredIds.contains(id)) {
            log.warn("Ignoring attempt to re-register request ID: {}", id);
        }
        else {
            registeredIds.add(id);

            log.debug("Registered request for ID: {}", id);
        }
    }

    public synchronized void deregister(final Request req) {
        assert req != null;

        UUID id = (UUID) req.getId();

        if (registeredIds.remove(id)) {
            log.debug("Dereegistered request for ID: {}", id);
        }
        else {
            log.warn("Ignoring attempt to deregister unregistered request ID: {}", id);
        }
    }
    
    //
    // ResponseInspector
    //
    
    public synchronized Object getRequestId(final Object message) {
        if (message instanceof Message) {
            UUID id = ((Message)message).getCorrelationId();

            if (registeredIds.contains(id)) {
                return id;
            }
        }

        return null;
    }

    //
    // TODO: Figure out wtf this is for...
    //
    
    public ResponseType getResponseType(final Object message) {
        return ResponseType.WHOLE;
    }
}