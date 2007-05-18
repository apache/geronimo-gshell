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

package org.apache.geronimo.gshell.server.telnet;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import net.wimpi.telnetd.net.Connection;
import net.wimpi.telnetd.net.ConnectionEvent;
import net.wimpi.telnetd.io.BasicTerminalIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jline.Terminal;
import jline.ConsoleReader;

import org.apache.geronimo.gshell.console.JLineConsole;
import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.Shell;
import org.apache.geronimo.gshell.InteractiveShell;

/**
 * Adapter to integrate GShell as a TelnetD shell.
 *
 * @version $Rev$ $Date$
 */
public class ShellImpl
    implements net.wimpi.telnetd.shell.Shell
{
    private Log log = LogFactory.getLog(this.getClass());

    //
    // Shell
    //
    
    public void run(final Connection connection) {
        try {
            doRun(connection);
        }
        catch (Exception e) {
            log.error("Unhandled exception", e);
        }
    }

    private void doRun(final Connection connection) throws Exception {
        assert connection != null;
        
        connection.addConnectionListener(this);

        BasicTerminalIO terminalIO = connection.getTerminalIO();
        Terminal term = new TelnetTerminal(terminalIO);
        IO io = new IO(createInputStream(terminalIO), createOutputStream(terminalIO));

        ConsoleReader reader = new ConsoleReader(io.inputStream, io.out, /* bindings */ null, term);
        JLineConsole console = new JLineConsole(io, reader);
        Shell shell = new Shell(console.getIO());

        InteractiveShell interp = new InteractiveShell(console, shell);
        interp.run();
    }

    private InputStream createInputStream(final BasicTerminalIO io) {
        return new InputStream() {
            public int read() throws IOException {
                return io.read();
            }
        };
    }

    private OutputStream createOutputStream(final BasicTerminalIO io) {
        return new OutputStream() {
            public void write(int b) throws IOException {
                io.write((byte)b);
            }
        };
    }

    //
    // ConnectionListener
    //
    
    public void connectionIdle(final ConnectionEvent event) {
        assert event != null;

        log.info("Connection went idle: " + event);
    }

    public void connectionTimedOut(final ConnectionEvent event) {
        assert event != null;

        log.info("Connection timed out: " + event);
    }

    public void connectionLogoutRequest(final ConnectionEvent event) {
        assert event != null;

        log.info("Connection logout request: " + event);
    }

    public void connectionSentBreak(final ConnectionEvent event) {
        assert event != null;
        
        log.info("Connection sent break: " + event);
    }

    //
    // Factory Access
    //

    public static net.wimpi.telnetd.shell.Shell createShell() {
        return new ShellImpl();
    }
}
