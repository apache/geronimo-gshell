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
import org.apache.commons.lang.NullArgumentException;
import org.apache.geronimo.gshell.Shell;
import org.apache.geronimo.gshell.InteractiveShell;
import org.apache.geronimo.gshell.command.CommandException;
import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.console.Console;
import org.apache.geronimo.gshell.console.ConsoleFactory;
import org.apache.xbean.finder.ResourceFinder;

import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

//
// NOTE: Some bits lifted from XBean Telnet module
//

/**
 * ???
 *
 * @version $Id$
 */
public class ShellServer
{
    private static final Log log = LogFactory.getLog(ShellServer.class);

    private final ConsoleFactory consoleFactory;

    public ShellServer() throws Exception {
        ResourceFinder resourceFinder = new ResourceFinder("META-INF/");
        Map<String, Class> resourcesMap = resourceFinder.mapAvailableImplementations(ConsoleFactory.class);

        //
        // HACK: Just for now hardcode this
        //
        String typename = "telnet";

        Class type = resourcesMap.get(typename);
        if (type == null) {
            throw new CommandException("Could not load ConsoleFactory of type: " + typename);
        }

        log.debug("Using console factory type: " + type);
        try {
            this.consoleFactory = (ConsoleFactory)type.newInstance();
            log.debug("Using console factory: " + this.consoleFactory);
        }
        catch (Exception e) {
            throw new CommandException("Failed to create ConsoleFactory of type: " + typename, e);
        }
    }

    public void service(final Socket socket) throws CommandException, IOException {
        if (socket == null) {
            throw new NullArgumentException("socket");
        }

        if (log.isDebugEnabled()) {
            log.debug("Servicing socket: " + socket);
        }

        try {
            service(socket.getInputStream(), socket.getOutputStream());
        }
        finally {
            // Socket always non-null
            socket.close();
        }
    }

    public void service(final InputStream input, final OutputStream output) throws CommandException, IOException {
        if (input == null) {
            throw new NullArgumentException("input");
        }
        if (output == null) {
            throw new NullArgumentException("output");
        }

        //
        // TODO: Need to figure out how to get the logging stream for this Shell to use
        //       the given IO streams
        //

        IO io = null;
        try {
            Console console = consoleFactory.create(input, output);

            io = console.getIO();
            Shell shell = new Shell(io);

            InteractiveShell interp = new InteractiveShell(console, shell);
            interp.run();
        }
        catch (Exception e) {
            throw new CommandException(e);
        }
        finally {
            if (io != null) {
                io.close();
            }
        }
    }
}
