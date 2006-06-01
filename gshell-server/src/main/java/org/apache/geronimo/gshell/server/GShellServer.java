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
import org.apache.geronimo.gshell.GShell;
import org.apache.geronimo.gshell.InteractiveGShell;
import org.apache.geronimo.gshell.console.IO;

import org.apache.xbean.terminal.telnet.TelnetInputStream;
import org.apache.xbean.terminal.telnet.TelnetPrintStream;

import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//
// NOTE: Some bits lifted from XBean Telnet module
//

/**
 * ???
 *
 * @version $Id$
 */
public class GShellServer
{
    private static final Log log = LogFactory.getLog(GShellServer.class);

    public void service(final Socket socket) throws IOException {
        if (socket == null) {
            throw new IllegalArgumentException("Socket is null");
        }

        if (log.isDebugEnabled()) {
            log.debug("Servicing socket: " + socket);
        }

        try {
            service(socket.getInputStream(), socket.getOutputStream());
        }
        finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    public void service(final InputStream in, final OutputStream out) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("Input is null");
        }
        if (out == null) {
            throw new IllegalArgumentException("Output is null");
        }

        IO io = null;

        //
        // TODO: Need to figure out how to get the logging stream for this GShell to use
        //       the given IO streams
        //

        try {
            //
            // TODO: Abstract Telnet specifics; support other protocols (ie. SSH)
            //

            //
            // TODO: Need access to the Terminal... NVT4J
            //

            io = new IO(new TelnetInputStream(in, out), new TelnetPrintStream(out));
            GShell shell = new GShell(io);

            InteractiveGShell interp = new InteractiveGShell(io, shell);
            interp.run();
        }
        finally {
            if (io != null) {
                io.close();
            }
        }
    }
}
