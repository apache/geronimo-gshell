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

package org.apache.geronimo.gshell.remote.message;

import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides handling for {@link Message} messages.
 *
 * @version $Rev$ $Date$
 */
@Component(role=MessageHandler.class)
public class MessageHandler
    extends IoHandlerAdapter
{
    //
    // TODO: Shall we make this a filter?
    //
    
    private final Logger log = LoggerFactory.getLogger(getClass());

    public MessageHandler(final MessageVisitor visitor) {
        assert visitor != null;

        this.visitor = visitor;
    }
    
    private MessageVisitor visitor;

    public void setVisitor(final MessageVisitor visitor) {
        assert visitor != null;
        
        this.visitor = visitor;
    }

    @Override
    public void sessionCreated(final IoSession session) throws Exception {
        // log.debug("Session created: {}", session);

        MessageVisitor.BINDER.bind(session, visitor);
    }

    @Override
    public void sessionOpened(final IoSession session) throws Exception {
        // log.debug("Session opened: {}", session);
    }

    @Override
    public void sessionClosed(final IoSession session) throws Exception {
        // log.debug("Session closed: {}", session);

        MessageVisitor.BINDER.unbind(session);
    }

    @Override
    public void messageReceived(final IoSession session, final Object obj) throws Exception {
        // log.debug("Message received: {}, message: {}", session, obj);
        
        if (obj instanceof Message) {
            Message msg = (Message)obj;

            //
            // TODO: Change the visit* methods to take a session and a visitor to avoid needing to attach to the message
            //
            
            // Attach the session to the context
            msg.setSession(session);
            msg.freeze();

            // Hand over to visitor for processing
            MessageVisitor v = MessageVisitor.BINDER.lookup(session);
            msg.process(v);
        }
        else {
            throw new InvalidMessageException(obj);
        }
    }

    @Override
    public void exceptionCaught(final IoSession session, final Throwable cause) throws Exception {
        //
        // TODO: Need to handle Exception muck, and send faul messages back to clients ?
        //

        log.error("Unhandled exception: " + cause, cause);
    }
}