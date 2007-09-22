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

package org.apache.geronimo.gshell.remote.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ThreadFactory} which automatically generates thread names based off of a
 * pre-configured basename passed in during construction and a unique index.
 *
 * @version $Rev$ $Date$
 */
public class NamedThreadFactory
    implements ThreadFactory
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String name;

    private final ThreadGroup group;

    private final AtomicLong counter = new AtomicLong(0);

    public NamedThreadFactory(final String name, final ThreadGroup group) {
        assert name != null;
        assert group != null;

        this.name = name;

        this.group = group;
    }

    public NamedThreadFactory(final String name) {
        this(name, Thread.currentThread().getThreadGroup());
    }

    public NamedThreadFactory(final Class type) {
        this(type.getSimpleName());
    }

    public NamedThreadFactory(final Class type, final String suffix) {
        this(type.getSimpleName() + "-" + suffix);
    }
    
    public Thread newThread(final Runnable task) {
        assert task != null;

        String id = name + "-" + counter.getAndIncrement();

        Thread t = new Thread(group, task, id);
        
        configure(t);

        log.debug("Created thread: {}", t);

        return t;
    }

    protected void configure(final Thread t) {
        t.setDaemon(true);
    }
}