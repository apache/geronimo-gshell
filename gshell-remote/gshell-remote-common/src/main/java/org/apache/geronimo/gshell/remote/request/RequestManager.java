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

import org.apache.geronimo.gshell.remote.message.Message;
import org.apache.geronimo.gshell.remote.util.SessionAttributeBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages per-session state and timeouts for requests.
 *
 * @version $Rev$ $Date$
 */
public class RequestManager
{
    public static final SessionAttributeBinder<RequestManager> BINDER = new SessionAttributeBinder<RequestManager>(RequestManager.class);

    private transient final Logger log = LoggerFactory.getLogger(getClass());

    private transient final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);

    private final Map<Message.ID,Request> requests = Collections.synchronizedMap(new HashMap<Message.ID, Request>());

    private final Map<Request,TimeoutTask> timeouts = Collections.synchronizedMap(new HashMap<Request,TimeoutTask>());
    
    //
    // TODO: Lock on Request.getMutex(), and/or change the mutex to a read/write lock?
    //

    public boolean contains(final Message.ID id) {
        assert id != null;

        return requests.containsKey(id);
    }

    public boolean contains(final Request request) {
        assert request != null;

        return contains(request.getId());
    }

    public void add(final Request request) {
        assert request != null;

        if (contains(request)) {
            throw new DuplicateRequestException(request);
        }

        Message.ID id = request.getId();

        if (log.isTraceEnabled()) {
            log.trace("Adding: {}", request);
        }
        else {
            log.debug("Adding: {}", id);
        }

        requests.put(id, request);
    }

    public Request get(final Message.ID id) {
        assert id != null;

        return requests.get(id);
    }

    public Request remove(final Message.ID id) {
        assert id != null;

        log.debug("Removing: {}", id);

        return requests.remove(id);
    }

    public void clear() {
        int l;

        l = requests.size();

        if (l > 0) {
            log.warn("Purging " + l + " request(s)");
        }

        requests.clear();

        l = timeouts.size();

        if (l > 0) {
            log.warn("Purging " + l + " timeouts(s)");
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

        Message.ID id = request.getId();

        if (request != get(id)) {
            throw new InvalidRequestMappingException(id);
        }

        if (log.isTraceEnabled()) {
            log.trace("Scheduling: {}", request);
        }
        else {
            log.debug("Scheduling: {}", id);
        }

        TimeoutTask task = new TimeoutTask(request);

        ScheduledFuture<?> tf = scheduler.schedule(task, request.getTimeout(), request.getTimeoutUnit());

        task.setTimeoutFuture(tf);

        timeouts.put(request, task);
    }

    public void cancel(final Request request) {
        assert request != null;

        Message.ID id = request.getId();

        TimeoutTask task = timeouts.remove(request);

        if (task == null) {
            throw new MissingRequestTimeoutException(id);
        }

        if (remove(id) != request) {
            throw new InvalidRequestMappingException(id);
        }

        if (log.isTraceEnabled()) {
            log.trace("Canceling: {}", request);
        }
        else {
            log.debug("Canceling: {}", id);
        }

        ScheduledFuture<?> sf = task.getTimeoutFuture();

        if (sf != null) {
            sf.cancel(false);
        }
    }

    private void timeout(final Request request) {
        assert request != null;

        Message.ID id = request.getId();

        if (log.isTraceEnabled()) {
            log.trace("Triggering: {}", request);
        }
        else {
            log.debug("Triggering: {}", id);
        }

        TimeoutTask task = timeouts.remove(request);

        if (task == null) {
            throw new MissingRequestTimeoutException(id);
        }

        if (remove(id) != request) {
            throw new InvalidRequestMappingException(id);
        }

        // If the request has not been signaled, then its a timeout :-(
        if (!request.isSignaled()) {
            request.timeout();
        }
    }

    private void timeoutAll() {
        log.debug("Timing out all pending requests");
        
        Request[] requests = timeouts.keySet().toArray(new Request[timeouts.size()]);

        for (Request request : requests) {
            timeout(request);
        }
    }

    public void close() {
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
}