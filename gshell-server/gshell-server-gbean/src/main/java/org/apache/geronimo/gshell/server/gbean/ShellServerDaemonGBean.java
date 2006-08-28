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

package org.apache.geronimo.gshell.server.gbean;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;

import org.apache.geronimo.gshell.server.SocketServerDaemon;
import org.apache.geronimo.gshell.server.SocketServerDaemon.SocketHandler;
import org.apache.geronimo.gshell.server.ShellServer;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.net.Socket;

/**
 * Shell server daemon GBean.
 *
 * @version $Rev$ $Date$
 */
public class ShellServerDaemonGBean
    implements ShellServerDaemon, GBeanLifecycle
{
    private static final Log log = LogFactory.getLog(ShellServerDaemonGBean.class);

    private SocketServerDaemon daemon;

    //
    // Attributes
    //

    private int port = 5057;

    public void setPort(final int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    //
    // GBeanLifecycle
    //

    public void doStart() throws Exception {
        if (daemon != null) {
            log.warn("Already started; ignoring start request");
            return;
        }

        log.info("Starting");

        SocketHandler handler = new SocketHandler() {
            ShellServer server = new ShellServer();

            public void handle(final Socket socket) throws Exception {
                assert socket != null;

                server.service(socket);
            }
        };

        daemon = new SocketServerDaemon(getPort(), handler);
        daemon.start();

        log.info("Started");
    }

    public void doStop() throws Exception {
        if (daemon == null) {
            log.warn("Not started; ignoring stop request");
            return;
        }

        log.info("Stopping");

        daemon.stop();
        daemon = null;

        log.info("Stopped");
    }

    public void doFail() {
        log.warn("Service failed; stopping");

        try {
            doStop();
        }
        catch (Exception e) {
            log.warn("Stop after failure failed; ignorning", e);
        }
    }

    //
    // GBeanInfo
    //

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder factory = GBeanInfoBuilder.createStatic(
            "Shell Server Daemon",
            ShellServerDaemonGBean.class);

        factory.addInterface(ShellServerDaemon.class);

        factory.addAttribute("port", Integer.TYPE, true, true);

        GBEAN_INFO = factory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
