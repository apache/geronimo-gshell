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

package org.apache.geronimo.gshell.server.ssh;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.server.TerminalSupport;

/**
 * SSH <a href="http://jline.sf.net">JLine</a> terminal implementation
 * backed up by a <a href="http://sourceforge.net/projects/sshtools">SSH Tools Daemon</a>.
 *
 * @version $Rev$ $Date$
 */
public class SshTerminal
    extends TerminalSupport
{
    private final IO io;

    public SshTerminal(final InputStream input, final OutputStream output) throws IOException {
        //
        // TODO:
        //
        
        this.io =  new IO(createInputStream(), createOutputStream());
    }

    public int getTerminalWidth() {
        throw new Error("TODO");
    }

    public int getTerminalHeight() {
        throw new Error("TODO");
    }
    
    private InputStream createInputStream() {
        return new InputStream() {
            public int read() throws IOException {
                throw new Error("TODO");
            }
        };
    }

    private OutputStream createOutputStream() {
        return new OutputStream() {
            public void write(final int i) throws IOException {
                throw new Error("TODO");
            }
        };
    }
    
    public IO getIO() {
        return io;
    }
}
