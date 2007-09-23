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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides handling for {@link Message} messages.
 *
 * @version $Rev$ $Date$
 */
public class MessageHandler
    extends IoHandlerAdapter
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private MessageVisitor visitor;

    protected MessageHandler() {}

    public MessageHandler(final MessageVisitor visitor) {
        this.visitor = visitor;
    }

    protected void setVisitor(final MessageVisitor visitor) {
        assert visitor != null;
        
        this.visitor = visitor;
    }

    protected MessageVisitor getVisitor() {
        if (visitor == null) {
            throw new IllegalStateException("Message visitor not bound");
        }

        return visitor;
    }

    @Override
    public void sessionCreated(final IoSession session) throws Exception {
        MessageVisitor.BINDER.bind(session, getVisitor());
    }

    @Override
    public void sessionClosed(final IoSession session) throws Exception {
        MessageVisitor.BINDER.unbind(session);
    }

    @Override
    public void messageReceived(final IoSession session, final Object obj) throws Exception {
        if (obj instanceof Message) {
            Message msg = (Message)obj;

            // Attach the session to the message (which is needed for methods like reply())
            msg.setSession(session);
            msg.freeze();
            
            // Hand over to visitor for processing
            MessageVisitor visitor = MessageVisitor.BINDER.lookup(session);

            msg.process(session, visitor);
        }
        else {
            throw new InvalidMessageException(obj);
        }
    }

    @Override
    public void exceptionCaught(final IoSession session, final Throwable cause) throws Exception {
        //
        // TODO: Need to handle Exception muck, and send fault messages back to clients ?
        //

        log.error("Unhandled exception: " + cause, cause);
    }
}