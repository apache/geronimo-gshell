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

package org.apache.geronimo.gshell.whisper.stream;

import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides stream I/O handling.
 *
 * @version $Rev$ $Date$
 */
public class SessionStreamFilter
    extends IoFilterAdapter
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Setup the input and output streams for the session.
     */
    @Override
    public void sessionCreated(final NextFilter nextFilter, final IoSession session) throws Exception {
        SessionInputStream.BINDER.bind(session, new SessionInputStream());
        SessionOutputStream.BINDER.bind(session, new SessionOutputStream(session));

        nextFilter.sessionCreated(session);
    }

    /**
     * Close the input and output streams for the session.
     */
    @Override
    public void sessionClosed(final NextFilter nextFilter, final IoSession session) throws Exception {
        IOUtil.close(SessionInputStream.BINDER.unbind(session));
        IOUtil.close(SessionOutputStream.BINDER.unbind(session));

        nextFilter.sessionClosed(session);
    }

    /**
     * Handles write stream messages.
     */
    @Override
    public void messageReceived(final NextFilter nextFilter, final IoSession session, final Object message) throws Exception {
        if (message instanceof StreamMessage) {
            StreamMessage msg = (StreamMessage) message;

            SessionInputStream in = SessionInputStream.BINDER.lookup(session);

            in.write(msg);

            // There is no need to pass on this message to any other filters, they have no use for it...
        }
        else {
            nextFilter.messageReceived(session, message);
        }
    }
}