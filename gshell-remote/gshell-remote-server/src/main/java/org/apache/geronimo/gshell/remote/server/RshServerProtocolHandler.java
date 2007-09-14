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

package org.apache.geronimo.gshell.remote.server;

import org.apache.geronimo.gshell.remote.RshProtocolHandlerSupport;
import org.apache.geronimo.gshell.remote.message.Message;
import org.apache.geronimo.gshell.remote.message.MessageVisitor;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.codehaus.plexus.component.annotations.Component;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
@Component(role=IoHandler.class, hint="rsh-server")
public class RshServerProtocolHandler
    extends RshProtocolHandlerSupport
{
    private MessageVisitor visitor;

    public MessageVisitor getVisitor() {
        return visitor;
    }

    public void setVisitor(final MessageVisitor visitor) {
        this.visitor = visitor;
    }

    public void messageReceived(final IoSession session, final Object message) throws Exception {
        assert session != null;
        assert message != null;

        super.messageReceived(session, message);

        Message msg = (Message)message;

        msg.setAttachment(session);

        msg.setAttachment(session);

        if (visitor != null) {
            msg.process(visitor);
        }
    }
}