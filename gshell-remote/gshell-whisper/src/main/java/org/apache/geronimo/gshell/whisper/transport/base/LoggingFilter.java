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

package org.apache.geronimo.gshell.whisper.transport.base;

import org.apache.geronimo.gshell.whisper.util.SessionAttributeBinder;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs all MINA protocol events to a {@link Logger}.
 *
 * @version $Rev$ $Date$
 */
public class LoggingFilter
    extends IoFilterAdapter
{
    private static final SessionAttributeBinder<Logger> LOGGER = new SessionAttributeBinder<Logger>(LoggingFilter.class, "logger");

    private static final SessionAttributeBinder<String> PREFIX = new SessionAttributeBinder<String>(LoggingFilter.class, "prefix");

    private Logger getLogger(final IoSession session) {
        assert session != null;

        Logger logger;

        try {
            logger = LOGGER.lookup(session);
        }
        catch (SessionAttributeBinder.NotBoundException e) {
            logger = LoggerFactory.getLogger(session.getHandler().getClass());

            if (!PREFIX.isBound(session)) {
                String prefix = "[" + session.getRemoteAddress() + "]";
                PREFIX.bind(session, prefix);
            }

            LOGGER.bind(session, logger);
        }

        return logger;
    }

    private void debug(final IoSession session, final String message) {
        assert session != null;
        assert message != null;

        Logger log = getLogger(session);
        if (log.isDebugEnabled()) {
            log.debug("{} {}", PREFIX.lookup(session), message);
        }
    }

    private void trace(final IoSession session, final String message, final Object detail) {
        assert session != null;
        assert message != null;
        assert detail != null;

        Logger log = getLogger(session);
        if (log.isTraceEnabled()) {
            log.trace("{} {}: {}", new Object[] { PREFIX.lookup(session), message, detail });
        }
    }

    private void warn(final IoSession session, final String message, final Throwable cause) {
        assert session != null;
        assert message != null;
        assert cause != null;

        Logger log = getLogger(session);
        if (log.isWarnEnabled()) {
            log.warn(PREFIX.lookup(session) + " " + message + ": " + cause, cause);
        }
    }

    public void sessionCreated(final NextFilter nextFilter, final IoSession session) {
        assert nextFilter != null;
        assert session != null;

        debug(session, "CREATED");

        nextFilter.sessionCreated(session);
    }

    public void sessionOpened(final NextFilter nextFilter, final IoSession session) {
        assert nextFilter != null;
        assert session != null;

        debug(session, "OPENED");

        nextFilter.sessionOpened(session);
    }

    public void sessionClosed(final NextFilter nextFilter, final IoSession session) {
        assert nextFilter != null;
        assert session != null;

        debug(session, "CLOSED");

        nextFilter.sessionClosed(session);
    }

    public void sessionIdle(final NextFilter nextFilter, final IoSession session, final IdleStatus status) {
        assert nextFilter != null;
        assert session != null;
        assert status != null;

        trace(session, "IDLE", status);

        nextFilter.sessionIdle(session, status);
    }

    public void exceptionCaught(final NextFilter nextFilter, final IoSession session, final Throwable cause) {
        assert nextFilter != null;
        assert session != null;
        assert cause != null;

        warn(session, "EXCEPTION", cause);

        nextFilter.exceptionCaught(session, cause);
    }

    public void messageReceived(final NextFilter nextFilter, final IoSession session, final Object message) {
        assert nextFilter != null;
        assert session != null;
        assert message != null;

        trace(session, "RECEIVED", message);

        nextFilter.messageReceived(session, message);
    }

    public void messageSent(final NextFilter nextFilter, final IoSession session, final Object message) {
        assert nextFilter != null;
        assert session != null;
        assert message != null;

        trace(session, "SENT", message);

        nextFilter.messageSent(session, message);
    }

    public void filterWrite(final NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest) {
        assert nextFilter != null;
        assert session != null;
        assert writeRequest != null;

        trace(session, "WRITE", writeRequest);

        nextFilter.filterWrite(session, writeRequest);
    }

    public void filterClose(final NextFilter nextFilter, final IoSession session) throws Exception {
        assert nextFilter != null;
        assert session != null;

        debug(session, "CLOSE");

        nextFilter.filterClose(session);
    }
}
