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

import java.net.Socket;
import java.net.ServerSocket;

//
// NOTE: Some bits lifted from XBean Telnet module
//

/**
 * Daemon service which listens for socket connections and
 * spawns client shells via {@link GShellServer}.
 *
 * @version $Id$
 */
public class GShellDaemon
    implements Runnable
{
    public static final int DEFAULT_PORT = 5057;

    private static final Log log = LogFactory.getLog(GShellDaemon.class);

    private final GShellServer server;

    private final int port;

    private boolean background;

    private ServerSocket serverSocket;

    private boolean running = false;

    public GShellDaemon(final int port, final boolean background) {
        this.port = port;
        this.background = background;

        this.server = new GShellServer();
    }

    public GShellDaemon(final int port) {
        this(port, true);
    }

    public GShellDaemon() {
        this(DEFAULT_PORT);
    }

    public void start() throws Exception {
        if (running) {
            throw new IllegalStateException("Already started");
        }

        log.info("Starting...");

        running = true;

        serverSocket = new ServerSocket(port, 20);
        Thread d = new Thread(this);
        d.setName("GShell Daemon@" + d.hashCode());
        d.setDaemon(true);
        d.start();

        log.info("Started");

        //
        // FIXME: This is broken... not what I had wanted at all :-(
        //

        // Wait for one client session and then return
        if (!background) {
            log.debug("Waiting for job to finish");

            running = false;
            d.join();
        }
    }

    public void stop() throws Exception {
        running = false;
    }

    public void service(final Socket socket) {
        if (socket == null) {
            throw new IllegalArgumentException("Socket is null");
        }

        log.info("Starting new thread for client: " + socket);

        Thread d = new Thread(new Runnable() {
            public void run() {
                try {
                    server.service(socket);
                }
                catch (Throwable e) {
                    log.error("Service failure", e);
                }
                finally {
                    try {
                        if (socket != null) {
                            socket.close();
                        }
                    }
                    catch (Throwable t) {
                        log.error("Failure while closing socket; ignoring", t);
                    }
                }
            }
        });

        d.setName("GShell@" + d.hashCode());
        d.setDaemon(true);
        d.start();
    }

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
}