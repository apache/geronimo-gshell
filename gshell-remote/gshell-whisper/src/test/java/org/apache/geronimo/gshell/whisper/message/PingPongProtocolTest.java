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

package org.apache.geronimo.gshell.whisper.message;

import java.net.URI;
import java.util.Map;

import org.apache.geronimo.gshell.whisper.transport.Session;
import org.apache.geronimo.gshell.whisper.transport.Transport;
import org.apache.geronimo.gshell.whisper.transport.TransportFactory;
import org.apache.geronimo.gshell.whisper.transport.TransportFactoryLocator;
import org.apache.geronimo.gshell.whisper.transport.TransportServer;
import org.apache.geronimo.gshell.whisper.SpringTestSupport;
import org.apache.mina.common.IoSession;
import org.apache.mina.handler.demux.DemuxingIoHandler;
import org.apache.mina.handler.demux.MessageHandler;

/**
 * Tests a simple PING -> PONG message protocol over the VM transport.
 *
 * @version $Rev$ $Date$
 */
public class PingPongProtocolTest
    extends SpringTestSupport
{
    private static void log(Object msg) {
        System.out.println(">>> [" + Thread.currentThread().getName() + "] " + msg);
    }

    private static void sleep(int seconds) throws InterruptedException {
        log("Sleeping for: " + seconds);
        Thread.sleep(seconds * 1000);
    }

    public static class PingMessage
        extends BaseMessage
    {
        public PingMessage() {}
    }

    public static class PongMessage
        extends BaseMessage
    {
        public PongMessage() {}
    }


    public static class PingHandler
        implements MessageHandler<PingMessage>
    {
        public void messageReceived(IoSession session, PingMessage message) throws Exception {
            log(message);

            sleep(2);

            session.write(new PingMessage()).join();
        }
    }

    public static class PongHandler
        implements MessageHandler<PongMessage>
    {
        public void messageReceived(IoSession session, PongMessage message) throws Exception {
            log(message);

            session.write(new PingMessage()).join();
        }
    }

    public static class PingPongProtocolHandler
        extends DemuxingIoHandler
    {
        public void sessionCreated(final IoSession session) throws Exception {
            log("Registering handlers");

            addMessageHandler(PingMessage.class, new PingHandler());

            addMessageHandler(PongMessage.class, new PongHandler());

            synchronized (System.err) {
                log("Handlers:");

                for (Map.Entry entry : getMessageHandlerMap().entrySet()) {
                    log("     " + entry.getKey() + "     " + entry.getValue());
                }
            }
        }

        /*
        public void sessionOpened(IoSession session) throws Exception {
            if (!TRANSPORT.isBound(session)) {
                Transport transport = new TransportAdapter(session);

                TRANSPORT.bind(session, transport);

                synchronized (System.err) {
                    log("BOUND Transport adapter: " + transport + ", for session: " + session);
                    new Throwable().printStackTrace();
                }
            }
        }
        */

        public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
            synchronized (System.err) {
                log("ERROR: session=" + session + ", cause=" + cause);

                if (cause != null) {
                    cause.printStackTrace();
                }

                session.close();
            }
        }
    }

    public static class PingPongClientHandler
        extends PingPongProtocolHandler
    {
        // Empty
    }

    public static class PingPongServerHandler
        extends PingPongProtocolHandler
    {
        // Empty
    }

    private TransportFactoryLocator locator;

    private TransportFactoryLocator getLocator() {
        TransportFactoryLocator locator = (TransportFactoryLocator) applicationContext.getBean("transportFactoryLocator");
        assertNotNull(locator);

        return locator;
    }

    protected void onSetUp() throws Exception {
        locator = getLocator();
    }

    public void testPingPong() throws Exception {
        URI uri = new URI("vm://local:1");

        log("Locator: " + locator);

        TransportFactory factory = locator.locate(uri);

        log("Factory: " + factory);

        TransportServer server = factory.bind(uri, new PingPongServerHandler());

        log("Server: " + server);

        Transport client = factory.connect(uri, null, new PingPongClientHandler());

        log("Client: " + client);

        Session session = client.getSession();

        sleep(2);

        session.send(new PingMessage()).join();
        
        sleep(10);

        client.close();

        server.close();
    }
}
