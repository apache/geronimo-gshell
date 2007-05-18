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
import java.io.IOException;

import net.wimpi.telnetd.io.BasicTerminalIO;

import org.apache.geronimo.gshell.server.TerminalSupport;

/**
 * JLine support for Telnetd shells.
 *
 * @version $Rev$ $Date$
 */
public class TelnetTerminal
    extends TerminalSupport
{
    private BasicTerminalIO io;

    public TelnetTerminal(final BasicTerminalIO io) {
        assert io != null;

        this.io = io;
    }

    public int getTerminalWidth() {
        return io.getColumns();
    }

    public int getTerminalHeight() {
        return io.getRows();
    }

    public int readVirtualKey(final InputStream in) throws IOException {
        assert in != null;

        int c = readCharacter(in);
        
        switch (c) {
            case BasicTerminalIO.UP:
                return CTRL_P;

            case BasicTerminalIO.DOWN:
                return CTRL_N;

            case BasicTerminalIO.LEFT:
                return CTRL_B;

            case BasicTerminalIO.RIGHT:
                return CTRL_F;

            //
            // TODO: HOME & END, backspace/delete
            //
        }
        
        if (c > 128) {
            throw new IOException("UTF-8 not supported");
        }

        return c;
    }
}
