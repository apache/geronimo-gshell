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

package org.apache.geronimo.gshell.remote.transport;

import org.apache.geronimo.gshell.remote.message.Message;
import org.apache.geronimo.gshell.remote.message.MessageResponseInspector;
import org.apache.geronimo.gshell.remote.message.MessageVisitor;
import org.apache.geronimo.gshell.remote.message.WriteStreamMessage;
import org.apache.geronimo.gshell.remote.stream.SessionInputStream;
import org.apache.geronimo.gshell.remote.stream.SessionOutputStream;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.reqres.Request;
import org.apache.mina.filter.reqres.Response;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the basic protocol handling for clients and servers.
 *
 * @version $Rev$ $Date$
 */
@Component(role=ProtocolHandler.class)
public class ProtocolHandler
    implements IoHandler
{
    protected Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    protected MessageResponseInspector responseInspector;

    //
    // TODO: Might be able to get this puppy injected...
    //
    
    protected MessageVisitor visitor;

    public MessageVisitor getVisitor() {
        return visitor;
    }

    public void setVisitor(final MessageVisitor visitor) {
        this.visitor = visitor;
    }

    public MessageResponseInspector getResponseInspector() {
        return responseInspector;
    }

    //
    // Stream Access
    //

    private void setInputStream(final IoSession session, final SessionInputStream in) {
        assert session != null;
        assert in != null;

        Object obj = session.getAttribute(Transport.INPUT_STREAM);

        if (obj != null) {
            throw new IllegalStateException("Input stream already bound");
        }

        session.setAttribute(Transport.INPUT_STREAM, in);

        log.debug("Bound input stream: {}", in);
    }

    private SessionInputStream getInputStream(final IoSession session) {
        assert session != null;

        SessionInputStream in = (SessionInputStream) session.getAttribute(Transport.INPUT_STREAM);

        if (in == null) {
            throw new IllegalStateException("Input stream not bound");
        }

        return in;
    }

    private SessionInputStream removeInputStream(final IoSession session) {
        assert session != null;

        return (SessionInputStream) session.removeAttribute(Transport.INPUT_STREAM);
    }

    private void setOutputStream(final IoSession session, final SessionOutputStream out) {
        assert session != null;
        assert out != null;

        Object obj = session.getAttribute(Transport.OUTPUT_STREAM);

        if (obj != null) {
            throw new IllegalStateException("Output stream already bound");
        }

        session.setAttribute(Transport.OUTPUT_STREAM, out);

        log.debug("Bound output stream: {}", out);
    }

    private SessionOutputStream getOutputStream(final IoSession session) {
        assert session != null;

        SessionOutputStream out = (SessionOutputStream) session.getAttribute(Transport.OUTPUT_STREAM);

        if (out == null) {
            throw new IllegalStateException("Output stream not bound");
        }


        return out;
    }

    private SessionOutputStream removeOutputStream(final IoSession session) {
        assert session != null;

        return (SessionOutputStream) session.removeAttribute(Transport.OUTPUT_STREAM);
    }

    //
    // IoHandler
    //

    public void sessionCreated(final IoSession session) throws Exception {
        log.debug("Session created: {}", session);
    }

    public void sessionOpened(final IoSession session) throws Exception {
        assert session != null;

        log.debug("Session opened: {}", session);

        //
        // Once the session has been opened, bind streams to the session context.
        //

        setInputStream(session, new SessionInputStream());
        
        setOutputStream(session, new SessionOutputStream(session));
    }

    public void sessionClosed(final IoSession session) throws Exception {
        assert session != null;

        log.debug("Session closed: {}", session);

        IOUtil.close(removeInputStream(session));
        
        IOUtil.close(removeOutputStream(session));
    }

    public void sessionIdle(final IoSession session, final IdleStatus status) throws Exception {
        assert session != null;

        log.debug("Session idle: {}, status: {}", session, status);

        if (status == IdleStatus.READER_IDLE) {
            log.warn("Read timeout");
        }
    }

    public void messageReceived(final IoSession session, final Object obj) throws Exception {
        assert session != null;
        assert obj != null;

        log.debug("Message received: {}", obj);

        //
        // TODO: Need to handle Exception muck, and send faul messages back to clients
        //

        if (obj instanceof Message) {
            //
            // This is the main protocol action, set the session, freeze the message and
            // then process the message with our visitor
            //

            Message msg = (Message)obj;

            msg.setSession(session);
            msg.freeze();

            if (msg instanceof WriteStreamMessage) {
                //
                // HACK: See if this fucking works...
                //

                SessionInputStream in = getInputStream(session);

                in.write((WriteStreamMessage)msg);
            }
            else if (visitor != null) {
                msg.process(visitor);
            }
            else {
                log.warn("Unable to process message because vistor has not been bound; ignoring");
            }
        }
        else if (obj instanceof Response) {
            //
            // Secondardy is to handle deregistration of request/resposne messages
            //

            Response resp = (Response)obj;

            Request req = resp.getRequest();

            responseInspector.deregister(req);
        }
        else {
            log.error("Unhandled message: {}", obj);
        }
    }

    public void messageSent(final IoSession session, final Object obj) throws Exception {
        assert session != null;

        log.debug("Message sent: {}", obj);

        if (obj instanceof Request) {
            //
            // When request messages are sent, we need to register them with the response inspector
            // so that when a resposne comes back we know how to correlate it with its request.
            //

            Request req = (Request) obj;

            responseInspector.register(req);
        }
    }

    public void exceptionCaught(final IoSession session, final Throwable cause) throws Exception {
        assert session != null;
        assert cause != null;

        log.error("Unhandled error: " + cause, cause);

        session.close();
    }
}