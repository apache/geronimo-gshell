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

package org.apache.geronimo.gshell.lookup;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.factory.AbstractComponentFactory;
import org.codehaus.plexus.component.factory.ComponentInstantiationException;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for lookup factory implementations.
 *
 * @version $Rev$ $Date$
 */
public class LookupFactorySupport
    extends AbstractComponentFactory
{
    protected Logger log = LoggerFactory.getLogger(getClass());

    protected final ThreadLocal holder = new ThreadLocal();

    public synchronized void set(final Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Instance can not be null");
        }

        Object prev = get();
        if (prev == null) {
            log.trace("Registered instance: {}", obj);
        }
        else {
            log.trace("Replacing previous instance with: {}", obj);
        }
        
        holder.set(obj);
    }

    public synchronized Object get() {
        return holder.get();
    }

    public Object newInstance(ComponentDescriptor descriptor, ClassRealm classRealm, PlexusContainer container) throws ComponentInstantiationException {
        Object obj = get();

        if (obj == null) {
            throw new IllegalStateException("Instance not registered");
        }
        
        log.trace("Handing out: {}", obj);

        return obj;
    }
}