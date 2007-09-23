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

package org.apache.geronimo.gshell.whisper.message;

import org.apache.geronimo.gshell.whisper.session.SessionAttributeBinder;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class MessageHandler
    extends IoHandlerAdapter
{
    protected static final SessionAttributeBinder<MessageVisitor> VISITOR = new SessionAttributeBinder<MessageVisitor>(MessageVisitor.class);

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected MessageVisitor visitor;

    public MessageHandler(final MessageVisitor visitor) {
        this.visitor = visitor;
    }

    @Override
    public void sessionCreated(final IoSession session) throws Exception {
        VISITOR.bind(session, visitor);
    }

    @Override
    public void sessionClosed(final IoSession session) throws Exception {
        VISITOR.unbind(session);
    }

    @Override
    public void messageReceived(final IoSession session, final Object obj) throws Exception {
        if (obj instanceof Message) {
            Message msg = (Message)obj;

            // Attach the session to the message (which is needed for methods like reply())
            msg.setSession(session);
            msg.freeze();
            
            // Hand over to visitor for processing
            MessageVisitor visitor = VISITOR.lookup(session);
            msg.process(session, visitor);
        }
        else {
            throw new MessageException("Invalid message: " + obj);
        }
    }
}