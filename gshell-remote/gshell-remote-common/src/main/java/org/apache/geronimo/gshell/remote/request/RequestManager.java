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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.geronimo.gshell.common.tostring.ToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.remote.message.Message;
import org.apache.geronimo.gshell.remote.session.SessionAttributeBinder;
import org.apache.geronimo.gshell.remote.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class RequestManager
{
    public static final SessionAttributeBinder<RequestManager> BINDER = new SessionAttributeBinder<RequestManager>(RequestManager.class);

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Map<Message.ID,Registration> registrations = new HashMap<Message.ID, Registration>();

    private final ScheduledExecutorService scheduler;

    //
    // TODO: Use a better locking scheme...
    //
    
    private final Lock lock = new ReentrantLock();

    public RequestManager() {
        ThreadFactory tf = new NamedThreadFactory(getClass());
        
        scheduler = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() + 1, tf);
    }

    private Registration get(final Message.ID id) {
        assert id != null;

        Registration reg = registrations.get(id);

        if (reg == null) {
            throw new NotRegisteredException(id);
        }

        return reg;
    }

    private Registration remove(final Message.ID id) {
        assert id != null;

        Registration reg = registrations.remove(id);

        if (reg == null) {
            throw new NotRegisteredException(id);
        }

        return reg;
    }

    public void register(final Request request) {
        assert request != null;

        lock.lock();

        try {
            Message.ID id = request.getId();

            if (registrations.containsKey(id)) {
                throw new DuplicateRegistrationException(id);
            }

            Registration reg = new Registration(request);

            registrations.put(id, reg);

            log.debug("Registered: {}", reg);
        }
        finally {
            lock.unlock();
        }
    }

    public Request lookup(final Message.ID id) {
        assert id != null;

        lock.lock();

        try {
            Registration reg = get(id);

            return reg.request;
        }
        finally {
            lock.unlock();
        }
    }

    public Request deregister(final Message.ID id) {
        assert id != null;

        lock.lock();

        try {
            Registration reg = remove(id);

            reg.deactivate();

            log.debug("Deregistered: {}", reg);

            return reg.request;
        }
        finally {
            lock.unlock();
        }
    }

    public void activate(final Message.ID id) {
        assert id != null;

        lock.lock();

        try {
            Registration reg = get(id);
            
            reg.activate();

            log.debug("Activated: {}", reg);
        }
        catch (NotRegisteredException e) {
            log.debug("Ignoring activation; request not registered: {}", id);
        }
        finally {
            lock.unlock();
        }
    }

    public void deactivate(final Message.ID id) {
        assert id != null;

        lock.lock();

        try {
            Registration reg = get(id);

            reg.deactivate();

            log.debug("Deactivated: {}", reg);
        }
        catch (NotRegisteredException e) {
            log.debug("Ignoring deactivation; request not registered: {}", id);
        }
        finally {
            lock.unlock();
        }
    }

    private void timeout(final Message.ID id) {
        assert id != null;

        lock.lock();

        try {
            Registration reg = remove(id);

            reg.timeout();

            log.debug("Timed out: {}", reg);
        }
        catch (NotRegisteredException e) {
            log.debug("Ignoring timeout; request not registered: {}", id);
        }
        catch (TimeoutAbortedException e) {
            log.debug("Timeout aborted: " + e.getMessage());
        }
        finally {
            lock.unlock();
        }
    }

    public void close() {
        lock.lock();

        try {
            if (!registrations.isEmpty()) {
                log.warn("Timing out remaining {} registrations", registrations.size());

                for (Registration reg : registrations.values()) {
                    timeout(reg.request.getId());
                }
            }

            //
            // FIXME: This causes some problems when a rsh client closes, like:
            //
            //        java.security.AccessControlException: access denied (java.lang.RuntimePermission modifyThread)
            //
            // scheduler.shutdown();
        }
        finally {
            lock.unlock();
        }
    }

    private enum RegistrationState
    {
        PENDING,
        ACTIVE,
        DEACTIVE,
        TIMEDOUT
    }

    private class Registration
    {
        public final Request request;

        public RegistrationState state = RegistrationState.PENDING;

        private ScheduledFuture<?> timeoutFuture;

        public Registration(final Request request) {
            assert request != null;

            this.request = request;
        }

        public void activate() {
            if (state != RegistrationState.PENDING) {
                log.debug("Can not activate, state is not PENDING, found: {}", state);
            }
            else {
                Runnable task = new Runnable() {
                    public void run() {
                        RequestManager.this.timeout(request.getId());
                    }
                };

                timeoutFuture = scheduler.schedule(task, request.getTimeout(), request.getTimeoutUnit());

                state = RegistrationState.ACTIVE;
            }
        }

        public void deactivate() {
            if (state != RegistrationState.ACTIVE) {
                log.debug("Can not deactivate; state is not ACTIVE, found: {}", state);
            }
            else if (timeoutFuture.cancel(false)) {
                timeoutFuture = null;

                state = RegistrationState.DEACTIVE;
            }
            else {
                log.warn("Unable to cancel registration timeout: {}", this);
            }
        }

        public void timeout() {
            Message.ID id = request.getId();

            if (timeoutFuture.isCancelled()) {
                throw new TimeoutAbortedException("Timeout has been canceled: " + id);
            }
            else if (request.isSignaled()) {
                throw new TimeoutAbortedException("Request has been singled: " + id);
            }
            else {
                request.timeout();

                state = RegistrationState.TIMEDOUT;
            }
        }

        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("id", request.getId())
                    .append("state", state)
                    .toString();
        }
    }

    public class NotRegisteredException
        extends RequestException
    {
        public NotRegisteredException(final Message.ID id) {
            super(id);
        }
    }

    public class DuplicateRegistrationException
        extends RequestException
    {
        public DuplicateRegistrationException(final Message.ID id) {
            super(id);
        }
    }

    public class TimeoutAbortedException
        extends RequestException
    {
        public TimeoutAbortedException(final String msg) {
            super(msg);
        }
    }
}