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

package org.apache.geronimo.gshell.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link ThreadFactory} which automatically generates thread names based off of a
 * pre-configured basename passed in during construction and a unique index.
 *
 * @version $Rev$ $Date$
 */
public class NamedThreadFactory
    implements ThreadFactory
{
    private final String baseName;

    private final ThreadGroup group;

    private final AtomicLong counter = new AtomicLong(0);

    public NamedThreadFactory(final String baseName, final ThreadGroup group) {
        assert baseName != null;
        assert group != null;

        this.baseName = baseName;
        this.group = group;
    }

    public NamedThreadFactory(final String baseName) {
        this(baseName, Thread.currentThread().getThreadGroup());
    }

    public NamedThreadFactory(final Class type) {
        this(type.getSimpleName());
    }

    public NamedThreadFactory(final Class type, final String suffix) {
        this(type.getSimpleName() + "-" + suffix);
    }

    public String getBaseName() {
        return baseName;
    }

    public ThreadGroup getGroup() {
        return group;
    }

    public long current() {
        return counter.get();
    }

    //
    // ThreadFactory
    //

    public Thread newThread(final Runnable task) {
        assert task != null;

        Thread t = new Thread(group, task, createName());
        
        configure(t);

        return t;
    }

    protected String createName() {
        return baseName + "-" + counter.getAndIncrement();
    }

    protected void configure(final Thread t) {
        t.setDaemon(true);
    }
}