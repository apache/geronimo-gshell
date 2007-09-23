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
import org.apache.geronimo.gshell.remote.message.rsh.ConnectMessage;
import org.apache.geronimo.gshell.remote.message.rsh.LoginMessage;
import org.apache.geronimo.gshell.remote.message.rsh.OpenShellMessage;
import org.apache.geronimo.gshell.remote.session.SessionAttributeBinder;

/**
 * Provides an abstraction layer for message processing.
 *
 * @version $Rev$ $Date$
 */
public interface MessageVisitor
{
    //
    // FIXME: Abstract me...
    //
    
    SessionAttributeBinder<MessageVisitor> BINDER = new SessionAttributeBinder<MessageVisitor>(MessageVisitor.class);

    void visitConnect(ConnectMessage msg) throws Exception;

    void visitLogin(LoginMessage msg) throws Exception;

    void visitEcho(EchoMessage msg) throws Exception;

    void visitOpenShell(OpenShellMessage msg) throws Exception;

    void visitCloseShell(CloseShellMessage msg) throws Exception;

    void visitExecute(ExecuteMessage msg) throws Exception;
}