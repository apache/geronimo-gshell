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

package org.apache.geronimo.gshell.server;

import java.io.InputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jline.Terminal;

import org.apache.geronimo.gshell.console.IO;

/**
 * Provides support for server-based <a href="http://jline.sf.net">JLine</a> terminal implementations.
 *
 * @version $Rev$ $Date$
 */
public abstract class TerminalSupport
    extends Terminal
{
    protected Log log = LogFactory.getLog(TerminalSupport.class);
    
    public void initializeTerminal() throws Exception {
    }
    
    public boolean isSupported() {
        return true;
    }
    
    public void disableEcho() {
        // TODO
    }
    
    public void enableEcho() {
        // TODO
    }
    
    public boolean isEchoEnabled() {
        return false;
    }
    
    public boolean getEcho() {
        return false;
    }

    //
    // NOTE: Added as workaround in bug introduced in JLine 0.9.91, should be fixed in 0.9.92+
    //
    
    public InputStream getDefaultBindings() {
        InputStream in = super.getDefaultBindings();
        if (in == null) {
            in = Terminal.class.getResourceAsStream("keybindings.properties");
        }
        
        return in;
    }

    //
    // NOTE: Copied (and modified) from jline.UnixTerminal
    //

    public static final short ARROW_START = 27;

    public static final short ARROW_PREFIX = 91;

    public static final short ARROW_LEFT = 68;

    public static final short ARROW_RIGHT = 67;

    public static final short ARROW_UP = 65;

    public static final short ARROW_DOWN = 66;

    public static final short HOME_CODE = 72;

    public static final short END_CODE = 70;
    
    public static final short O_PREFIX = 79;
    
    public int readCharacter(final InputStream in) throws IOException {
        int c = in.read();
        
        if (log.isDebugEnabled()) {
            String ch;
            if (c == 0xd) {
                ch = "\\n";
            }
            else {
                ch = new String(new char[] { (char)c });
            }
            
            log.debug("Read char: " + ch + " (x" + Integer.toHexString(c) + ")");
        }
        
        return c;
    }
    
    public int readVirtualKey(final InputStream in) throws IOException {
        assert in != null;

        int c = readCharacter(in);

        //
        // TODO: Need to check if this is correct... arrow handling is a tad off
        //

        log.info("Read virtual key: " + c);
        
        // in Unix terminals, arrow keys are represented by
        // a sequence of 3 characters. E.g., the up arrow
        // key yields 27, 91, 68

        if (c == ARROW_START) {
            c = readCharacter(in);

            if (c == ARROW_PREFIX || c == O_PREFIX) {
                c = readCharacter(in);

                switch (c) {
                    case ARROW_UP:
                        return CTRL_P;

                    case ARROW_DOWN:
                        return CTRL_N;

                    case ARROW_LEFT:
                        return CTRL_B;

                    case ARROW_RIGHT:
                        return CTRL_F;

                    case HOME_CODE:
                        return CTRL_A;

                    case END_CODE:
                        return CTRL_E;
                }
            }
        }
        
        if (c > 128) {
            throw new IOException("UTF-8 not supported");
        }

        return c;
    }
}
