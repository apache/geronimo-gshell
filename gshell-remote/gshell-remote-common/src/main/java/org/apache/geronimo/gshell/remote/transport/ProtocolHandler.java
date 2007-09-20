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

package org.apache.geronimo.gshell.remote.transport;

import org.apache.geronimo.gshell.remote.message.Message;
import org.apache.geronimo.gshell.remote.message.MessageVisitor;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the basic protocol handling for clients and servers.
 *
 * @version $Rev$ $Date$
 */
@Component(role=ProtocolHandler.class)
public class ProtocolHandler
    implements IoHandler
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private MessageVisitor visitor;

    public MessageVisitor getVisitor() {
        return visitor;
    }

    public void setVisitor(final MessageVisitor visitor) {
        this.visitor = visitor;
    }

    //
    // TODO: Do we need to stuff the visitor into the session context?
    //

    public void sessionCreated(IoSession session) throws Exception {
        log.debug("Session created: {}", session);
    }

    public void sessionOpened(final IoSession session) throws Exception {
        log.debug("Session opened: {}", session);
    }

    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        log.debug("Session idle: {}, status: {}", session, status);
    }

    public void sessionClosed(final IoSession session) throws Exception {
        log.debug("Session closed: {}", session);
    }

    public void messageReceived(final IoSession session, final Object obj) throws Exception {
        log.debug("Message received: {}, message: {}", session, obj);
        
        //
        // TODO: Need to handle Exception muck, and send faul messages back to clients
        //

        if (obj instanceof Message) {
            // This is the main protocol action, set the session, freeze the message and
            // then process the message with our visitor

            final Message msg = (Message)obj;

            msg.setSession(session);
            msg.freeze();

            //
            // TODO: Make sure we have a visitor... gotta have it really...
            //
            
            if (visitor != null) {
                msg.process(visitor);
            }
            else {
                log.warn("Unable to process message because vistor has not been bound; ignoring");
            }
        }
        else {
            log.error("Unhandled message: {}", obj);
        }
    }

    public void messageSent(IoSession session, Object message) throws Exception {
        log.debug("Message sent: {}, message: {}", session, message);
    }

    public void exceptionCaught(final IoSession session, final Throwable cause) throws Exception {
        log.error("Exception caught: " + session + ", cause: " + cause, cause);
    }
}