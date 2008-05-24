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

package org.apache.geronimo.gshell.io;

import jline.ConsoleReader;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Helper to prompt a user for information.
 *
 * @version $Rev$ $Date$
 */
public class PromptReader
{
    private char mask = '*';

    private final ConsoleReader reader;

    public PromptReader(final IO io) throws IOException {
        assert io != null;
        
        this.reader = new ConsoleReader(io.inputStream, new PrintWriter(io.outputStream, true), /*bindings*/ null, io.getTerminal());
    }

    public char getMask() {
        return mask;
    }

    public void setMask(final char mask) {
        this.mask = mask;
    }

    public String readLine(final String prompt) throws IOException {
        assert prompt != null;

        return reader.readLine(prompt);
    }

    public String readLine(final String prompt, final char mask) throws IOException {
        assert prompt != null;

        return reader.readLine(prompt, mask);
    }

    public String readPassword(final String prompt) throws IOException {
        assert prompt != null;
        
        return reader.readLine(prompt, mask);
    }
}
