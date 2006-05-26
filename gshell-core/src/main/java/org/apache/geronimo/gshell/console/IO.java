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

package org.apache.geronimo.gshell.console;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;

/**
 * Container for input/output handles.
 *
 * @version $Id$
 */
public class IO
{
    /**
     * Raw input stream.
     *
     * @see #in
     */
    public final InputStream inputStream;

    /**
     * Raw output stream.
     *
     * @see #out
     */
    public final PrintStream outputStream;

    /**
     * Raw error output stream.
     *
     * @see #err
     */
    public final PrintStream errorStream;

    /**
     * Prefered input reader.
     */
    public final Reader in;

    /**
     * Prefered output writer.
     */
    public final PrintWriter out;

    /**
     * Prefered error output writer.
     */
    public final PrintWriter err;

    /**
     * Construct a new IO container.
     *
     * @param in    The input steam; must not be null
     * @param out   The output stream; must not be null
     * @param err   The error output stream; must not be null
     */
    public IO(final InputStream in, final PrintStream out, final PrintStream err) {
        if (in == null) {
            throw new IllegalArgumentException("Input stream is null");
        }
        if (out == null) {
            throw new IllegalArgumentException("Output stream is null");
        }
        if (err == null) {
            throw new IllegalArgumentException("Error output stream is null");
        }

        this.inputStream = in;
        this.outputStream = out;
        this.errorStream = err;

        this.in = new InputStreamReader(in);
        this.out = new PrintWriter(out);
        this.err = new PrintWriter(err);
    }

    /**
     * Helper which uses current values from {@link System}.
     */
    public IO() {
        this(System.in, System.out, System.err);
    }

    /**
     * Flush both output streams.
     */
    public void flush() {
        out.flush();
        err.flush();
    }
}
