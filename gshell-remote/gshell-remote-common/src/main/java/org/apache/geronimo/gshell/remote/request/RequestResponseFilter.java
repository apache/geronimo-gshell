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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.geronimo.gshell.remote.message.Message;
import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides synchronous request/response messaging.
 *
 * @version $Rev$ $Date$
 */
public class RequestResponseFilter
    extends IoFilterAdapter
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ExecutorService executor;

    public RequestResponseFilter(final ExecutorService executor) {
        assert executor != null;

        this.executor = executor;
    }

    public RequestResponseFilter() {
        this(Executors.newCachedThreadPool());
    }

    /**
     * Set up the request manager instance for the session.
     */
    @Override
    public void sessionCreated(final NextFilter nextFilter, final IoSession session) throws Exception {
        RequestManager.bind(session, new RequestManager());

        nextFilter.sessionCreated(session);
    }

    /**
     * Close the request manager instance for the session.
     */
    @Override
    public void sessionClosed(final NextFilter nextFilter, final IoSession session) throws Exception {
        RequestManager manager = RequestManager.unbind(session);

        manager.close();
        
        nextFilter.sessionClosed(session);
    }

    /**
     * When a request is sent, register it with the request manager.
     */
    @Override
    public void filterWrite(final NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest) throws Exception {
        Object message = writeRequest.getMessage();

        if (message instanceof Request) {
            // Register the request with the manager
            Request request = (Request) message;

            RequestManager manager = RequestManager.lookup(session);
            manager.add(request);
        }

        nextFilter.filterWrite(session, writeRequest);
    }

    /**
     * When a response message has been received, cancel its timeout and signal the request.
     */
    @Override
    public void messageReceived(final NextFilter nextFilter, final IoSession session, final Object message) throws Exception {
        Message msg = null;

        Object id = null;

        if (message instanceof Message) {
            msg = (Message)message;
            
            id = msg.getCorrelationId();
        }

        // If we have a correlation id, then we can process the response
        if (id != null) {
            RequestManager manager = RequestManager.lookup(session);

            final Request request = manager.get(id);

            // Cancel the timeout
            manager.cancel(request);

            // Setup the response and signal the request
            final Response response = new Response(request, msg, Response.Type.WHOLE);

            // Signal could block so queue it
            Runnable task = new Runnable() {
                public void run() {
                    request.signal(response);
                }
            };

            executor.execute(task);

            // Do not pass on the response
        }
        else {
            nextFilter.messageReceived(session, message);
        }
    }

    /**
     * Once the reqeust message has been sent, schedule a timeout.
     */
    @Override
    public void messageSent(final NextFilter nextFilter, final IoSession session, final Object message) throws Exception {
        if (message instanceof Request) {
            Request request = (Request) message;

            RequestManager manager = RequestManager.lookup(session);

            manager.schedule(request);
        }

        nextFilter.messageSent(session, message);
    }
}