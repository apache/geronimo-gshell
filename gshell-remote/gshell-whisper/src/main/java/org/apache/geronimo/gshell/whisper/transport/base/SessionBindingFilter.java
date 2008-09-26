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

import org.apache.geronimo.gshell.whisper.transport.Session;
import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple filter which handles binding and unbinding the Whisper {@link Session} instance
 * when Mina creates and closes its native session.
 *
 * @version $Rev$ $Date$
 */
public class SessionBindingFilter
    extends IoFilterAdapter
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void sessionCreated(final NextFilter nextFilter, final IoSession session) throws Exception {
        Session s = Session.BINDER.bind(session, new SessionAdapter(session));

        log.trace("Bound: {}", s);

        nextFilter.sessionCreated(session);
    }

    @Override
    public void sessionClosed(final NextFilter nextFilter, final IoSession session) throws Exception {
        Session s = Session.BINDER.unbind(session);

        log.trace("Unbound: {}", s);

        nextFilter.sessionClosed(session);
    }
}