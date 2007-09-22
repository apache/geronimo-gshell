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

import org.apache.geronimo.gshell.remote.message.MessageHandler;
import org.apache.geronimo.gshell.remote.message.MessageVisitorSupport;
import org.apache.geronimo.gshell.remote.message.rsh.EchoMessage;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

/**
 * ???
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

    //
    // MessageVisitor
    //

    private class Visitor
        extends MessageVisitorSupport
    {
        public void visitEcho(final EchoMessage msg) throws Exception {
            assert msg != null;

            log.info("ECHO: {}", msg.getText());
        }
    }
}