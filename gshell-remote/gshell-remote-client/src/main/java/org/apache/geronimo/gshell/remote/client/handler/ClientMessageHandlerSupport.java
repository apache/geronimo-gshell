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

package org.apache.geronimo.gshell.remote.client.handler;

import org.apache.geronimo.gshell.whisper.message.Message;
import org.apache.geronimo.gshell.whisper.message.MessageHandlerSupport;
import org.apache.mina.common.IoSession;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public abstract class ClientMessageHandlerSupport<T extends Message>
    extends MessageHandlerSupport<T>
    implements ClientMessageHandler<T>
{
    protected ClientMessageHandlerSupport(final Message.Type type) {
        super(type);
    }

    public void messageReceived(final IoSession session, final T message) throws Exception {
        ClientSessionContext context = ClientSessionContext.BINDER.lookup(session);

        handle(session, context, message);
    }
}