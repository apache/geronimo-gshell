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

package org.apache.geronimo.gshell.whisper.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Helper to locate a {@link TransportFactory} instance.
 *
 * @version $Rev$ $Date$
 */
public class TransportFactoryLocatorImpl<T extends TransportFactory>
    implements TransportFactoryLocator<T>
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Set<TransportFactory> factories;

    private final Map<String,TransportFactory> factoryLookup = new HashMap<String,TransportFactory>();

    public Set<TransportFactory> getFactories() {
        return factories;
    }

    public void setFactories(final Set<TransportFactory> factories) {
        this.factories = factories;
    }

    // @PostConstruct
    public void init() {
        if (factories == null) {
            log.warn("No transport factories discovered");
            return;
        }

        log.debug("Discovered {} transport factories", factories.size());

        for (TransportFactory factory : factories) {
            String scheme = factory.getScheme();

            log.debug("Mapping transport scheme: {}", scheme);

            factoryLookup.put(scheme, factory);
        }
    }

    @SuppressWarnings({"unchecked"})
    public T locate(final URI location) throws TransportException {
        assert location != null;

        String scheme = location.getScheme();

        if (scheme == null) {
            throw new InvalidLocationException(location);
        }

        TransportFactory transportFactory = factoryLookup.get(scheme);
        if (transportFactory == null) {
            throw new InvalidLocationException(location, "No transport factory configured for scheme: " + scheme);
        }

        return (T)transportFactory;
    }
}