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

package org.apache.geronimo.gshell.remote.security;

import java.security.PublicKey;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.geronimo.gshell.remote.crypto.CryptoContext;
import org.apache.geronimo.gshell.remote.message.Message;
import org.apache.geronimo.gshell.remote.message.rsh.HandShakeMessage;
import org.apache.geronimo.gshell.remote.message.rsh.LoginMessage;
import org.apache.geronimo.gshell.remote.util.NamedThreadFactory;
import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides authentication and security support.
 *
 * @version $Rev$ $Date$
 */
@Component(role=SecurityFilter.class)
public class SecurityFilter
    extends IoFilterAdapter
{
    private static final String AUTHENTICATED_KEY = SecurityFilter.class.getName() + ".authenticated";

    private static final String REMOTE_PUBLIC_KEY_KEY = SecurityFilter.class.getName() + ".remotePublicKey";

    private static final String TIMEOUT_TASK_KEY = TimeoutTask.class.getName();
    
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private CryptoContext crypto;

    @Requirement
    private UserAuthenticator userAuthenticator;

    private final ScheduledThreadPoolExecutor scheduler;

    private final UUID securityToken;

    public SecurityFilter() throws Exception {
        ThreadFactory tf = new NamedThreadFactory(getClass());
        
        scheduler = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), tf);

        //
        // TODO: Create a token based on our public key er something, so we can better use it to determine session validitaty.
        //

        securityToken = UUID.randomUUID();
    }

    @Override
    public void sessionOpened(final NextFilter nextFilter, final IoSession session) throws Exception {
        assert nextFilter != null;
        assert session != null;

        // Schedule a task to timeout the handshake process
        scheduleTimeout(session);

        nextFilter.sessionOpened(session);
    }

    @Override
    public void messageReceived(final NextFilter nextFilter, final IoSession session, final Object obj) throws Exception {
        assert nextFilter != null;
        assert session != null;
        assert obj != null;

        UUID token = (UUID) session.getAttribute(AUTHENTICATED_KEY);

        // If the session is already authenticated, then pass on the message
        if (securityToken.equals(token)) {
            nextFilter.messageReceived(session, obj);
        }
        else if (token != null) {
            log.error("Invalid security token: {}", token);

            session.close();
        }
        else if (obj instanceof HandShakeMessage) {
            doHandShake(session, (HandShakeMessage)obj);
        }
        else if (obj instanceof LoginMessage) {
            doLogin(session, (LoginMessage)obj);
        }
        else {
            // If we get to here, then the message is not valid, so complain, then kill the session
            log.error("Unauthenticated message: {}", obj);

            session.close();
        }
    }

    private void setSession(final IoSession session, final Message msg) {
        assert session != null;
        assert msg != null;

        // Prep the message for reply, this is normally done by the protocol handler, but that hasn't a chance to fire at this point
        msg.setSession(session);
        msg.freeze();
    }

    private void doHandShake(final IoSession session, final HandShakeMessage msg) throws Exception {
        assert session != null;
        assert msg != null;

        log.debug("Processing handshake");

        setSession(session, msg);

        // Try to cancel the timeout task
        if (!cancelTimeout(session)) {
            log.warn("Aborting handshake processing; timeout has triggered");
        }
        else {
            PublicKey key = msg.getPublicKey();
            
            // Stuff the remote public key into the session
            session.setAttribute(REMOTE_PUBLIC_KEY_KEY, key);

            //
            // TODO: Do we want to pass the client back some token which it needs to put onto messages that are sent for more security?
            //
            
            // And then send back our public key to the remote client
            msg.reply(new HandShakeMessage.Result(crypto.getPublicKey()));

            // Don't wait on the write future

            // Schedule a task to timeout the login process
            scheduleTimeout(session);
        }
    }

    private void doLogin(final IoSession session, final LoginMessage msg) throws Exception {
        assert session != null;
        assert msg != null;

        log.debug("Processing login");

        setSession(session, msg);
        
        // Try to cancel the timeout task
        if (!cancelTimeout(session)) {
            log.warn("Aborting login processing; timeout has triggered");
        }
        else {
            String username = msg.getUsername();
            String password = msg.getPassword();

            if (!userAuthenticator.authenticate(username, password)) {
                log.error("Authentication failed for user: {}, at location: {}", username, session.getRemoteAddress());

                String reason = "Failed to authenticate";

                msg.reply(new LoginMessage.Failure(reason));
            }
            else {
                // Mark the session as authenticated
                session.setAttribute(AUTHENTICATED_KEY, securityToken);

                log.info("Successfull authentication for user: {}, at location: {}", username, session.getRemoteAddress());

                msg.reply(new LoginMessage.Success());

                // Don't wait on the write future
            }
        }
    }

    //
    // Timeout Support
    //

    private ScheduledFuture scheduleTimeout(final IoSession session, final long l, final TimeUnit unit) {
        assert session != null;

        ScheduledFuture task = scheduler.schedule(new TimeoutTask(session), l, unit);
        session.setAttribute(TIMEOUT_TASK_KEY, task);

        return task;
    }

    private ScheduledFuture scheduleTimeout(final IoSession session) {
        return scheduleTimeout(session, 30, TimeUnit.SECONDS);
    }
    
    private boolean cancelTimeout(final IoSession session) {
        assert session != null;

        ScheduledFuture timeoutTask = (ScheduledFuture) session.getAttribute(TIMEOUT_TASK_KEY);

        return timeoutTask.cancel(false);
    }

    /**
     * Task to timeout sessions which fail to handshake or authenticate in a timely manner.
     */
    private class TimeoutTask
        implements Runnable
    {
        private final IoSession session;

        public TimeoutTask(final IoSession session) {
            assert session != null;

            this.session = session;
        }

        public void run() {
            log.error("Timeout waiting for handshake or authentication from: " + session.getRemoteAddress());

            session.close();
        }
    }
}