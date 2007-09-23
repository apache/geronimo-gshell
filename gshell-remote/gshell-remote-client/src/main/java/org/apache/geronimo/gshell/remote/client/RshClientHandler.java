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

package org.apache.geronimo.gshell.remote.client;

import java.util.UUID;

import org.apache.geronimo.gshell.common.tostring.ToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.remote.message.MessageHandler;
import org.apache.geronimo.gshell.remote.message.MessageVisitorSupport;
import org.apache.geronimo.gshell.remote.message.rsh.EchoMessage;
import org.apache.geronimo.gshell.remote.session.SessionAttributeBinder;
import org.apache.mina.common.IoSession;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

/**
 * rovides the client-side message handling for the GShell rsh protocol.
 *
 * @version $Rev$ $Date$
 */
@Component(role=MessageHandler.class, hint="client")
public class RshClientHandler
    extends MessageHandler
    implements Initializable
{
    public RshClientHandler() {}

    public void initialize() throws InitializationException {
        setVisitor(new Visitor());
    }

    @Override
    public void sessionClosed(final IoSession session) throws Exception {
        assert session != null;

        SessionState state = SESSION_STATE.unbind(session);

        // If there is still state bound then clean it up
        if (state != null) {
            log.warn("Delinquent state detected: {}", state);

            try {
                state.destroy();
            }
            catch (Exception e) {
                log.warn("Failed to clean up after delinquent state", e);
            }
        }
    }

    //
    // SessionState
    //

    /**
     * Session binding helper for {@link SessionState} instances.
     */
    private static final SessionAttributeBinder<SessionState> SESSION_STATE = new SessionAttributeBinder<SessionState>(SessionState.class);

    /**
     * Container for various bits of client state we are tracking.
     */
    private class SessionState
    {
        public final UUID id;

        public SessionState(final UUID id) {
            this.id = id;
        }
        
        public void destroy() {}

        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("id", id)
                    .toString();
        }
    }
    
    //
    // MessageVisitor
    //

    private class Visitor
        extends MessageVisitorSupport
    {
        public void visitEcho(IoSession session, final EchoMessage msg) throws Exception {
            assert msg != null;

            log.info("ECHO: {}", msg.getText());
        }
    }
}