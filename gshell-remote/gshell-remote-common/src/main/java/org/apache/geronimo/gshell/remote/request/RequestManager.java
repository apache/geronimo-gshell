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

package org.apache.geronimo.gshell.remote.request;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class RequestManager
{
    private static final Logger log = LoggerFactory.getLogger(RequestManager.class);

    private transient final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);

    private final Map<Object,Request> requests = Collections.synchronizedMap(new HashMap<Object, Request>());

    private final Map<Request,TimeoutTask> timeouts = Collections.synchronizedMap(new HashMap<Request,TimeoutTask>());
    
    //
    // TODO: Lock on Request.getMutex()
    //

    public boolean contains(final Object id) {
        assert id != null;

        if (id instanceof Request) {
            throw new IllegalArgumentException("Expecting a request ID, not a request");
        }

        return requests.containsKey(id);
    }

    public boolean contains(final Request req) {
        assert req != null;

        return contains(req.getId());
    }

    public void add(final Request req) {
        assert req != null;

        if (contains(req)) {
            throw new DuplicateRequestException(req);
        }

        Object id = req.getId();

        log.debug("Adding request for ID: {}", id);

        requests.put(id, req);
    }

    public Request get(final Object id) {
        assert id != null;

        if (id instanceof Request) {
            throw new IllegalArgumentException("Expecting a request ID, not a request");
        }

        return requests.get(id);
    }

    public Request remove(final Object id) {
        assert id != null;

        if (id instanceof Request) {
            throw new IllegalArgumentException("Expecting a request ID, not a request");
        }

        log.debug("Removing request for ID: {}", id);

        return requests.remove(id);
    }

    public void clear() {
        int l;

        l = requests.size();

        if (l > 0) {
            log.warn("Clearing requests; purging " + l + " request(s)");
        }

        requests.clear();

        l = timeouts.size();

        if (l > 0) {
            log.warn("Clearing timeouts; purging " + l + " timeouts(s)");
        }

        timeouts.clear();
    }

    //
    // Timeouts
    //

    public void schedule(final Request request) {
        assert request != null;

        if (timeouts.containsKey(request)) {
            throw new DuplicateRequestException(request);
        }

        Object id = request.getId();

        if (request != get(id)) {
            throw new IllegalStateException("Found invalid request mapping for ID: " + id);
        }

        log.debug("Scheduling timeout for request ID: {}", id);

        TimeoutTask task = new TimeoutTask(request);

        ScheduledFuture<?> tf = scheduler.schedule(task, request.getTimeout(), request.getTimeoutUnit());

        task.setTimeoutFuture(tf);

        timeouts.put(request, task);
    }

    public void cancel(final Request request) {
        assert request != null;

        Object id = request.getId();

        TimeoutTask task = timeouts.remove(request);

        if (task == null) {
            throw new IllegalStateException("Request ID has no timeout bound: " + id);
        }

        if (remove(id) != request) {
            throw new IllegalStateException("Found invalid request mapping for ID: " + id);
        }

        log.debug("Canceling timeout for request ID: {}", id);

        ScheduledFuture<?> sf = task.getTimeoutFuture();

        if (sf != null) {
            sf.cancel(false);
        }
    }

    private void timeout(final Request request) {
        assert request != null;

        Object id = request.getId();

        log.debug("Triggering timeout for request ID: {}", id);

        TimeoutTask task = timeouts.remove(request);

        if (task == null) {
            throw new IllegalStateException("Request ID has no timeout bound: " + id);
        }

        if (remove(id) != request) {
            throw new IllegalStateException("Found invalid request mapping for ID: " + id);
        }

        log.debug("Canceling timeout for request ID: {}", id);

        // If the request has not been signaled, then its a timeout :-(
        if (!request.isSignaled()) {
            // noinspection ThrowableInstanceNeverThrown
            request.signal(new RequestTimeoutException(request));
        }
    }

    public void timeoutAll() {
        log.debug("Timing out all pending tasks");

        Request[] requests = timeouts.keySet().toArray(new Request[timeouts.size()]);

        for (Request request : requests) {
            timeout(request);
        }
    }

    public void close() {
        log.debug("Closing");

        timeoutAll();
        clear();
    }

    //
    // TimeoutTask
    //

    private class TimeoutTask
        implements Runnable
    {
        private final Request request;

        private ScheduledFuture<?> timeoutFuture;

        private TimeoutTask(final Request request) {
            assert request != null;

            this.request = request;
        }

        public void run() {
            timeout(request);
        }

        public void setTimeoutFuture(final ScheduledFuture<?> timeoutFuture) {
            assert timeoutFuture != null;

            this.timeoutFuture = timeoutFuture;
        }

        public ScheduledFuture<?> getTimeoutFuture() {
            return timeoutFuture;
        }
    }

    //
    // Session Access
    //
    
    public static RequestManager lookup(final IoSession session) {
        assert session != null;

        RequestManager manager = (RequestManager) session.getAttribute(RequestManager.class.getName());

        if (manager == null) {
            throw new IllegalStateException("Request manager not bound");
        }

        log.trace("Looked up: {}", manager);
        
        return manager;
    }

    public static void bind(final IoSession session, final RequestManager manager) {
        assert session != null;
        assert manager != null;

        if (session.containsAttribute(RequestManager.class.getName())) {
            throw new IllegalStateException("Request manager already bound");
        }
        
        session.setAttribute(RequestManager.class.getName(), manager);

        log.trace("Bound: {}", manager);
    }

    public static RequestManager unbind(final IoSession session) {
        assert session != null;

        RequestManager manager = (RequestManager) session.removeAttribute(RequestManager.class.getName());

        log.trace("Unbound: {}", manager);

        return manager;
    }
}