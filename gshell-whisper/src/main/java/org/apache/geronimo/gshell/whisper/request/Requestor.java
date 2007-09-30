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

package org.apache.geronimo.gshell.whisper.request;

import java.util.concurrent.TimeUnit;

import org.apache.geronimo.gshell.common.Duration;
import org.apache.geronimo.gshell.whisper.message.Message;
import org.apache.geronimo.gshell.whisper.transport.Transport;
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
    private static final Duration DEFAULT_TIMEOUT = new Duration(10, TimeUnit.SECONDS);

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final IoSession session;

    private Duration timeout;

    public Requestor(final IoSession session, final Duration timeout) {
        this.session = session;
        this.timeout = timeout;
    }

    public Requestor(final IoSession session) {
        this(session, DEFAULT_TIMEOUT);
    }

    public Requestor(final Transport transport) {
        this(transport.getSession(), DEFAULT_TIMEOUT);
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(final Duration timeout) {
        assert timeout != null;
        
        this.timeout = timeout;
    }

    public RequestWriteFuture submit(final Message msg, final Duration timeout) throws Exception {
        assert msg != null;
        assert timeout != null;

        RequestHandle req = new RequestHandle(msg, timeout.getValue(), timeout.getUnit());

        WriteFuture wf = session.write(req);

        return new RequestWriteFuture(wf, req);
    }

    public RequestWriteFuture submit(final Message msg) throws Exception {
        return submit(msg, timeout);
    }

    public Message request(final Message msg, final Duration timeout) throws Exception {
        RequestWriteFuture wf = submit(msg, timeout);

        RequestHandle req = wf.getRequest();

        ResponseHandle resp = req.awaitResponse();

        return resp.getMessage();
    }

    public Message request(final Message msg) throws Exception {
        return request(msg, timeout);
    }
    
    //
    // RequestWriteFuture
    //

    public class RequestWriteFuture
        implements WriteFuture
    {
        private final WriteFuture delegate;

        private final RequestHandle request;

        public RequestWriteFuture(final WriteFuture wf, final RequestHandle req) {
            this.delegate = wf;
            this.request = req;
        }

        public RequestHandle getRequest() {
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