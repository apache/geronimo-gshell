/*
 * Copyright 2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.gshell.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.NDC;

import java.net.Socket;
import java.net.ServerSocket;

//
// NOTE: Some bits lifted from XBean Telnet module
//

/**
 * Daemon service which listens for socket connections and spawns client threads.
 *
 * @version $Id$
 */
public class SocketServerDaemon
    implements Runnable
{
    private static final Log log = LogFactory.getLog(SocketServerDaemon.class);

    private final SocketHandler handler;

    private final int port;

    private ServerSocket serverSocket;

    private ThreadGroup threads = new ThreadGroup("SocketServerDaemon");

    private boolean running;

    public SocketServerDaemon(final int port, final SocketHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("Socket handler is null");
        }

        this.port = port;
        this.handler = handler;
    }

    public void start() throws Exception {
        if (running) {
            throw new IllegalStateException("Already started");
        }

        log.info("Starting...");

        running = true;

        serverSocket = new ServerSocket(port, 20);
        Thread d = new Thread(this);
        d.setName("SocketServerDaemon@" + d.hashCode());
        d.setDaemon(true);
        d.start();

        log.info("Started");
    }

    public void stop() throws Exception {
        if (!running) {
            throw new IllegalStateException("Not started");
        }

        serverSocket.close();
        serverSocket = null;

        running = false;
    }

    public void service(final Socket socket) {
        if (socket == null) {
            throw new IllegalArgumentException("Socket is null");
        }

        log.info("Starting new thread for client: " + socket);

        //
        // TODO: Maybe use a thread-pool here?
        //

        Thread d = new Thread(threads, new Runnable() {
            public void run() {
                NDC.push(socket.toString());

                try {
                    handler.handle(socket);
                }
                catch (Throwable e) {
                    log.error("Service failure", e);
                }
                finally {
                    try {
                        // Socket always non-null
                        socket.close();
                    }
                    catch (Throwable t) {
                        log.error("Failure while closing socket; ignoring", t);
                    }

                    NDC.pop();
                }
            }
        });

        d.setName("SocketHandler@" + d.hashCode());
        d.setDaemon(true);
        d.start();
    }

    //
    // Runnable
    //

    public void run() {
        log.info("Listening for connections on port: " + port);

        while (running) {
            try {
                Socket socket = serverSocket.accept();
                socket.setTcpNoDelay(true);

                if (running) {
                    service(socket);
                }
            }
            catch (Throwable t) {
                log.error("Unexpected; ignoring", t);
            }
        }
    }

    //
    // SocketHandler
    //

    /**
     * Allows custom processing for client socket connections.
     */
    public static interface SocketHandler
    {
        /**
         * Handle the client socket.
         *
         * @param socket    The client socket; never null
         *
         * @throws Exception
         */
        void handle(Socket socket) throws Exception;
    }
}