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

package org.apache.geronimo.gshell.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;

/**
 * Container for input/output handles.
 *
 * @version $Rev$ $Date$
 */
public class IO
{
    /**
     * Raw input stream.
     *
     * @see #in     For general usage, please use the reader.
     */
    public final InputStream inputStream;

    /**
     * Raw output stream.
     *
     * @see #out    For general usage, please use the writer.
     */
    public final OutputStream outputStream;

    /**
     * Raw error output stream.
     *
     * @see #err    For general usage, please use the writer.
     */
    public final OutputStream errorStream;

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
     * The verbosity setting, which commands (and framework) should inspect and respect when
     * spitting up output to the user.
     */
    private Verbosity verbosity = Verbosity.INFO;

    /**
     * Construct a new IO container.
     *
     * @param in    The input steam; must not be null
     * @param out   The output stream; must not be null
     * @param err   The error output stream; must not be null
     */
    public IO(final InputStream in, final OutputStream out, final OutputStream err) {
        assert in != null;
        assert out != null;
        assert err != null;

        this.inputStream = in;
        this.outputStream = out;
        this.errorStream = err;

        this.in = new InputStreamReader(in);
        this.out = new PrintWriter(out, true);
        this.err = new PrintWriter(err, true);
    }

    /**
     * Construct a new IO container.
     *
     * @param in    The input steam; must not be null
     * @param out   The output stream and error stream; must not be null
     */
    public IO(final InputStream in, final OutputStream out) {
        this(in, out, out);
    }

    /**
     * Helper which uses current values from {@link System}.
     */
    public IO() {
        this(System.in, System.out, System.err);
    }

    /**
     * Set the verbosity level.
     *
     * @param verbosity
     */
    public void setVerbosity(final Verbosity verbosity) {
        assert verbosity != null;
        
        this.verbosity = verbosity;
    }

    /**
     * Check if the verbosity level is set to {@link Verbosity#QUIET}.
     */
    public boolean isQuiet() {
        return verbosity == Verbosity.QUIET;
    }

    /**
     * Check if the verbosity level is set to {@link Verbosity#INFO}.
     */
    public boolean isInfo() {
        return verbosity == Verbosity.INFO;
    }

    /**
     * Check if the verbosity level is set to {@link Verbosity#VERBOSE}.
     */
    public boolean isVerbose() {
        return verbosity == Verbosity.VERBOSE;
    }

    /**
     * Check if the verbosity level is set to {@link Verbosity#DEBUG}.
     *
     * <p>For generaly usage, when debug output is required, it is better
     * to use the logging facility instead.
     */
    public boolean isDebug() {
        return verbosity == Verbosity.DEBUG;
    }

    /**
     * Flush both output streams.
     */
    public void flush() {
        out.flush();
        err.flush();
    }

    /**
     * Close all streams.
     */
    public void close() throws IOException {
        in.close();
        out.close();
        err.close();
    }

    //
    // Verbosity
    //

    /**
     * Defines the valid values of the {@link IO} containers verbosity settings.
     */
    public static enum Verbosity
    {
        QUIET,
        INFO,
        VERBOSE,
        DEBUG
    }
}
