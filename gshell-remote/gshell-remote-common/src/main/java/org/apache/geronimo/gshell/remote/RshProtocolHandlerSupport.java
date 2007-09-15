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

package org.apache.geronimo.gshell.remote;

import org.apache.geronimo.gshell.remote.message.Message;
import org.apache.geronimo.gshell.remote.message.MessageResponseInspector;
import org.apache.geronimo.gshell.remote.message.MessageVisitor;
import org.apache.geronimo.gshell.remote.stream.IoSessionInputStream;
import org.apache.geronimo.gshell.remote.stream.IoSessionOutputStream;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.reqres.Request;
import org.apache.mina.filter.reqres.Response;
import org.apache.mina.filter.reqres.ResponseInspector;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public abstract class RshProtocolHandlerSupport
    implements IoHandler
{
    public static final String STREAM_BASENAME = "org.apache.geronimo.gshell.remote.stream.";

    public static final String INPUT_STREAM = STREAM_BASENAME + "IN";

    public static final String OUTPUT_STREAM = STREAM_BASENAME + "OUT";

    public static final String ERROR_STREAM = STREAM_BASENAME + "ERR";

    protected Logger log = LoggerFactory.getLogger(getClass());

    protected MessageVisitor visitor;

    protected MessageResponseInspector responseInspector = new MessageResponseInspector(); 

    public MessageVisitor getVisitor() {
        return visitor;
    }

    public void setVisitor(final MessageVisitor visitor) {
        this.visitor = visitor;
    }

    public MessageResponseInspector getResponseInspector() {
        return responseInspector;
    }
    
    public void messageReceived(final IoSession session, final Object message) throws Exception {
        assert session != null;
        assert message != null;

        if (message instanceof Message) {
            Message msg = (Message)message;

            if (visitor != null) {
                msg.setAttachment(session);
                msg.process(visitor);
            }
        }
        else if (message instanceof Response) {
            Response resp = (Response)message;

            Request req = resp.getRequest();

            responseInspector.deregister(req);
        }
        else {
            log.error("Unhandled message: {}", message);
        }
    }

    //
    // IoHandler
    //

    public void sessionCreated(final IoSession session) throws Exception {
        log.info("Session created: {}", session);
    }

    public void sessionOpened(final IoSession session) throws Exception {
        log.info("Session opened: {}", session);

        IoSessionInputStream in = new IoSessionInputStream();
        session.setAttribute(INPUT_STREAM, in);

        IoSessionOutputStream out = new IoSessionOutputStream(session);
        session.setAttribute(OUTPUT_STREAM, out);

        //
        // TODO: Add err
        //
    }

    public void sessionClosed(final IoSession session) throws Exception {
        log.info("Session closed: {}", session);

        IoSessionInputStream in = (IoSessionInputStream) session.getAttribute(INPUT_STREAM);
        IOUtil.close(in);

        IoSessionOutputStream out = (IoSessionOutputStream) session.getAttribute(OUTPUT_STREAM);
        IOUtil.close(out);

        //
        // TODO: Add err
        //
    }

    public void sessionIdle(final IoSession session, final IdleStatus status) throws Exception {
        log.info("Session idle: {}, status: {}", session, status);

        /*
        if (status == IdleStatus.READER_IDLE) {
            throw new SocketTimeoutException("Read timeout");
        }
        */
    }

    public void exceptionCaught(final IoSession session, final Throwable cause) throws Exception {
        assert session != null;
        assert cause != null;

        log.error("Unhandled error: " + cause, cause);

        session.close();
    }

    public void messageSent(final IoSession session, final Object message) throws Exception {
        log.info("Message sent: {}", message);

        if (message instanceof Request) {
            Request req = (Request) message;

            responseInspector.register(req);
        }
    }
}