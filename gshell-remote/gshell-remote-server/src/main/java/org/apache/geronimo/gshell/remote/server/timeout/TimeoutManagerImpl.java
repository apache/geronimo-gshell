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

package org.apache.geronimo.gshell.remote.server.timeout;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import org.apache.geronimo.gshell.chronos.Duration;
import org.apache.geronimo.gshell.whisper.util.NamedThreadFactory;
import org.apache.geronimo.gshell.whisper.transport.Session;
import org.apache.geronimo.gshell.whisper.util.SessionAttributeBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

/**
 * Default {@link TimeoutManager} implementation.
 *
 * @version $Rev$ $Date$
 */
public class TimeoutManagerImpl
    implements TimeoutManager
{
    private static final SessionAttributeBinder<ScheduledFuture> TIMEOUT = new SessionAttributeBinder<ScheduledFuture>(TimeoutManager.class, "timeout");

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ScheduledThreadPoolExecutor scheduler;

    @PostConstruct
    public void init() {
        ThreadFactory tf = new NamedThreadFactory(getClass());
        scheduler = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), tf);
    }

    public ScheduledFuture scheduleTimeout(final Session session, final Duration timeout, final Runnable task) {
        assert session != null;
        assert timeout != null;
        assert task != null;

        log.debug("Scheduling timeout; session: {}, duration: {}", session, timeout);

        ScheduledFuture tf = scheduler.schedule(task, timeout.value, timeout.unit);

        TIMEOUT.rebind(session.getSession(), tf);

        return tf;
    }

    public boolean cancelTimeout(final Session session) {
        assert session != null;

        log.debug("Canceling timeout; session: {}", session);

        ScheduledFuture tf = TIMEOUT.lookup(session.getSession());

        return tf.cancel(false);
    }
}
