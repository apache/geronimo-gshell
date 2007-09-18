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

package org.apache.geronimo.gshell.remote.transport.tcp;

import org.apache.geronimo.gshell.remote.message.Message;
import org.apache.geronimo.gshell.remote.message.MessageResponseInspector;
import org.apache.geronimo.gshell.remote.message.MessageVisitor;
import org.apache.geronimo.gshell.remote.stream.IoSessionInputStream;
import org.apache.geronimo.gshell.remote.stream.IoSessionOutputStream;
import org.apache.geronimo.gshell.remote.transport.Transport;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.reqres.Request;
import org.apache.mina.filter.reqres.Response;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
@Component(role=TcpProtocolHandler.class)
public class TcpProtocolHandler
    implements IoHandler
{
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

    public void messageReceived(final IoSession session, final Object obj) throws Exception {
        assert session != null;
        assert obj != null;

        log.info("Message received: {}", obj);

        if (obj instanceof Message) {
            Message msg = (Message)obj;

            if (visitor != null) {
                msg.setSession(session);
                msg.freeze();
                msg.process(visitor);
            }
        }
        else if (obj instanceof Response) {
            Response resp = (Response)obj;

            Request req = resp.getRequest();

            responseInspector.deregister(req);
        }
        else {
            log.error("Unhandled message: {}", obj);
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
        session.setAttribute(Transport.INPUT_STREAM, in);

        IoSessionOutputStream out = new IoSessionOutputStream(session);
        session.setAttribute(Transport.OUTPUT_STREAM, out);

        //
        // TODO: Add err
        //
    }

    public void sessionClosed(final IoSession session) throws Exception {
        log.info("Session closed: {}", session);

        IoSessionInputStream in = (IoSessionInputStream) session.getAttribute(Transport.INPUT_STREAM);
        IOUtil.close(in);

        IoSessionOutputStream out = (IoSessionOutputStream) session.getAttribute(Transport.OUTPUT_STREAM);
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

    public void messageSent(final IoSession session, final Object obj) throws Exception {
        log.info("Message sent: {}", obj);

        if (obj instanceof Request) {
            Request req = (Request) obj;

            responseInspector.register(req);
        }
    }
}