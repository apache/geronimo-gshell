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

import org.apache.geronimo.gshell.remote.message.rsh.CloseShellMessage;
import org.apache.geronimo.gshell.remote.message.rsh.EchoMessage;
import org.apache.geronimo.gshell.remote.message.rsh.ExecuteMessage;
import org.apache.geronimo.gshell.remote.message.rsh.OpenShellMessage;
import org.apache.geronimo.gshell.remote.message.rsh.ConnectMessage;
import org.apache.geronimo.gshell.remote.message.rsh.LoginMessage;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides support for {@link MessageVisitor} implementations.
 *
 * @version $Rev$ $Date$
 */
public class MessageVisitorSupport
    implements MessageVisitor
{
    //
    // FIXME: Abstract me...
    //
    
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected MessageVisitorSupport() {}

    public void visitConnect(IoSession session, ConnectMessage msg) throws Exception {}

    public void visitLogin(IoSession session, LoginMessage msg) throws Exception {}

    public void visitEcho(IoSession session, EchoMessage msg) throws Exception {}

    public void visitOpenShell(IoSession session, OpenShellMessage msg) throws Exception {}

    public void visitCloseShell(IoSession session, CloseShellMessage msg) throws Exception {}

    public void visitExecute(IoSession session, ExecuteMessage msg) throws Exception {}
}