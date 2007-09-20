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

package org.apache.geronimo.gshell.remote.stream;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.IoSessionConfig;
import org.apache.mina.common.IoFilter;
import org.apache.mina.common.WriteRequest;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class SessionStreamFilter
    extends IoFilterAdapter
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ExecutorService executor;

    public SessionStreamFilter(final ExecutorService executor) {
        assert executor != null;

        this.executor = executor;
    }

    public SessionStreamFilter() {
        this(Executors.newCachedThreadPool());
    }

    //
    // TODO: See if we need to put the executor into the session context
    //

    public void sessionCreated(NextFilter nextFilter, IoSession session) throws Exception {
        log.debug("Binding session streams");

        SessionInputStream.bind(session, new SessionInputStream());

        SessionOutputStream.bind(session, new SessionOutputStream(session));

        nextFilter.sessionCreated(session);
    }

    /*
    public void sessionOpened(NextFilter nextFilter, IoSession session) throws Exception {
        IoSessionConfig config = session.getConfig();

        config.setWriteTimeout(60);
        config.setIdleTime(IdleStatus.READER_IDLE, 60);

        nextFilter.sessionOpened(session);
    }
    */
    
    public void sessionClosed(NextFilter nextFilter, IoSession session) throws Exception {
        log.debug("Unbinding session streams");

        IOUtil.close(SessionInputStream.unbind(session));
        
        IOUtil.close(SessionOutputStream.unbind(session));

        nextFilter.sessionClosed(session);
    }

    public void sessionIdle(NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
        log.debug("Session idle: {}, status: {}", session, status);

        nextFilter.sessionIdle(session, status);
    }

    public void exceptionCaught(NextFilter nextFilter, IoSession session, Throwable cause) throws Exception {
        log.debug("Exception caught: " + session + ", cause: " + cause, cause);

        nextFilter.exceptionCaught(session, cause);
    }

    public void messageReceived(final NextFilter nextFilter, final IoSession session, final Object message) throws Exception {
        if (message instanceof WriteStreamMessage) {
            final WriteStreamMessage msg = (WriteStreamMessage) message;

            final SessionInputStream in = SessionInputStream.lookup(session);

            Runnable task = new Runnable() {
                public void run() {
                    log.debug("Writing stream...");

                    in.write(msg);

                    log.debug("Done");
                }
            };

            executor.execute(task);

            // There is no need to pass on this message to any other filters, they have no use for it...
        }
        else {
            nextFilter.messageReceived(session, message);
        }
    }

    public void messageSent(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        Object message = writeRequest.getMessage();

        if (message instanceof WriteStreamMessage) {
            log.debug("Message sent: {}, msg: {}", session, message);

            //
            // TODO: Check the future's status?
            //
        }

        nextFilter.messageSent(session, writeRequest);
    }
}