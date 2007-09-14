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

package org.apache.geronimo.gshell.remote;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public abstract class RshProtocolHandlerSupport
    implements IoHandler
{
    protected Logger log = LoggerFactory.getLogger(getClass());

    public void sessionCreated(final IoSession session) throws Exception {
        log.info("Session created: {}", session);
    }

    public void sessionOpened(final IoSession session) throws Exception {
        log.info("Session opened: {}", session);
    }

    public void sessionClosed(final IoSession session) throws Exception {
        log.info("Session closed: {}", session);
    }

    public void sessionIdle(final IoSession session, final IdleStatus status) throws Exception {
        log.info("Session idle: {}, status: {}", session, status);
    }

    public void exceptionCaught(final IoSession session, final Throwable cause) throws Exception {
        assert session != null;
        assert cause != null;

        log.error("Unhandled error: " + cause, cause);

        session.close();
    }

    public void messageReceived(final IoSession session, final Object message) throws Exception {
        log.info("Message received: {}", message);
    }

    public void messageSent(final IoSession session, final Object message) throws Exception {
        log.info("Message sent: {}", message);
    }
}