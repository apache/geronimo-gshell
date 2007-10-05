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

package org.apache.geronimo.gshell.console;

import java.io.PrintWriter;
import java.io.IOException;

import jline.ConsoleReader;
import jline.Terminal;
import org.apache.geronimo.gshell.command.IO;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

/**
 * Helper to prompt a user for information.
 *
 * @version $Rev$ $Date$
 */
@Component(role= PromptReader.class, instantiationStrategy="per-lookup")
public class PromptReader
    implements Initializable
{
    @Requirement
    private Terminal terminal;

    @Requirement
    private IO io;

    private char mask = '*';

    private ConsoleReader reader;

    public char getMask() {
        return mask;
    }

    public void setMask(final char mask) {
        this.mask = mask;
    }

    public void initialize() throws InitializationException {
        try {
            reader = new ConsoleReader(io.inputStream, new PrintWriter(io.outputStream, true), /*bindings*/ null, terminal);
        }
        catch (IOException e) {
            throw new InitializationException("Failed to create reader", e);
        }
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
