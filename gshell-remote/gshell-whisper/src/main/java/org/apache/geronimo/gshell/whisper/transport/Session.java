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

package org.apache.geronimo.gshell.whisper.transport;

import org.apache.geronimo.gshell.chronos.Duration;
import org.apache.geronimo.gshell.whisper.message.Message;
import org.apache.geronimo.gshell.whisper.util.SessionAttributeBinder;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides the session for a transport connection.
 *
 * @version $Rev$ $Date$
 */
public interface Session
    extends Closeable
{
    SessionAttributeBinder<Session> BINDER = new SessionAttributeBinder<Session>(Session.class);

    IoSession getSession();

    void close();

    WriteFuture send(Object msg) throws Exception;

    Message request(Message msg, Duration timeout) throws Exception;

    Message request(Message msg) throws Exception;

    //
    // NOTE: This could handle setting the correlationId ???
    //
    // WriteFuture reply(Message reply, Message replyTo) throws Exception;

    InputStream getInputStream();

    OutputStream getOutputStream();

    OutputStream getErrorStream();
}