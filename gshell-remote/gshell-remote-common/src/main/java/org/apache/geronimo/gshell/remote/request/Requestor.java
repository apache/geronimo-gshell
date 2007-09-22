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

import java.util.concurrent.TimeUnit;

import org.apache.geronimo.gshell.remote.message.Message;
import org.apache.geronimo.gshell.remote.transport.Transport;
import org.apache.mina.common.IoFutureListener;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides support to send a request and receive it's response.
 *
 * @version $Rev$ $Date$
 */
public class Requestor
{
    public static final long DEFAULT_TIMEOUT = 10;

    public static final TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final IoSession session;

    private final long timeout;

    private final TimeUnit unit;

    private Requestor(final IoSession session, final long timeout, final TimeUnit unit) {
        this.session = session;
        this.timeout = timeout;
        this.unit = unit;
    }

    public Requestor(final IoSession session) {
        this(session, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT);
    }

    public Requestor(final Transport transport, final long timeout, final TimeUnit unit) {
        this(transport.getSession(), timeout, unit);
    }

    public Requestor(final Transport transport) {
        this(transport, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT);
    }

    public RequestWriteFuture submit(final Message msg, final long timeout, final TimeUnit unit) throws Exception {
        assert msg != null;

        Request req = new Request(msg, timeout, unit);

        WriteFuture wf = session.write(req);

        return new RequestWriteFuture(wf, req);
    }

    public RequestWriteFuture submit(final Message msg) throws Exception {
        return submit(msg, timeout, unit);
    }

    public Message request(final Message msg, final long timeout, final TimeUnit unit) throws Exception {
        assert msg != null;

        RequestWriteFuture wf = submit(msg, timeout, unit);

        Request req = wf.getRequest();

        Response resp = req.awaitResponse();

        return resp.getMessage();
    }

    public Message request(final Message msg) throws Exception {
        return request(msg, timeout, unit);
    }
    
    //
    // RequestWriteFuture
    //

    public class RequestWriteFuture
        implements WriteFuture
    {
        private final WriteFuture delegate;

        private final Request request;

        public RequestWriteFuture(final WriteFuture wf, final Request req) {
            this.delegate = wf;
            this.request = req;
        }

        public Request getRequest() {
            return request;
        }

        public boolean isWritten() {
            return delegate.isWritten();
        }

        public void setWritten(final boolean written) {
            delegate.setWritten(written);
        }

        public IoSession getSession() {
            return delegate.getSession();
        }

        public void join() {
            delegate.join();
        }

        public boolean join(final long timeout) {
            return delegate.join(timeout);
        }

        public boolean isReady() {
            return delegate.isReady();
        }

        public void addListener(final IoFutureListener listener) {
            delegate.addListener(listener);
        }

        public void removeListener(final IoFutureListener listener) {
            delegate.removeListener(listener);
        }
    }
}