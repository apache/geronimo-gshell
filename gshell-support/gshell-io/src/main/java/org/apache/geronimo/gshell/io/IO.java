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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;

import org.apache.geronimo.gshell.ansi.RenderWriter;
import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.helpers.MessageFormatter;

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
     * @param in            The input steam; must not be null
     * @param out           The output stream; must not be null
     * @param err           The error output stream; must not be null
     * @param autoFlush     True to enable auto-flushing off writers.
     */
    public IO(final InputStream in, final OutputStream out, final OutputStream err, final boolean autoFlush) {
        assert in != null;
        assert out != null;
        assert err != null;

        this.inputStream = in;
        this.outputStream = out;
        this.errorStream = err;

        this.in = new InputStreamReader(in);

        //
        // TODO: Once all user output is in i18n, then it would be more efficent to have the MessageSource
        //       be ANSI-aware instead of this...
        //


        this.out = new RenderWriter(outputStream, autoFlush);

        /// Don't rewrite the error stream if we have the same stream for out and error
        if (isSharedOutputStreams()) {
            this.err = this.out;
        }
        else {
            this.err = new RenderWriter(errorStream, autoFlush);
        }

        // this.out = new PrintWriter(out, autoFlush);
        // this.err = new PrintWriter(err, autoFlush);
    }

    /**
     * Construct a new IO container.
     *
     * @param in    The input steam; must not be null
     * @param out   The output stream; must not be null
     * @param err   The error output stream; must not be null
     */
    public IO(final InputStream in, final OutputStream out, final OutputStream err) {
        this(in, out, err, true);
    }

    /**
     * Construct a new IO container.
     *
     * @param in            The input steam; must not be null
     * @param out           The output stream and error stream; must not be null
     * @param autoFlush     True to enable auto-flushing off writers.
     */
    public IO(final InputStream in, final OutputStream out, final boolean autoFlush) {
        this(in, out, out, autoFlush);
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

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    //
    // FIXME: Find a better name for this method.
    //
    
    public boolean isSharedOutputStreams() {
        return outputStream == errorStream;
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
     * Returns the verbosity level.
     */
    public Verbosity getVerbosity() {
        return verbosity;
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

        // Only attempt to flush the err stream if we aren't sharing it with out
        if (!isSharedOutputStreams()) {
            err.flush();
        }
    }

    /**
     * Close all streams.
     */
    public void close() throws IOException {
        IOUtil.close(in);
        IOUtil.close(out);

        // Only attempt to close the err stream if we aren't sharing it with out
        if (!isSharedOutputStreams()) {
            IOUtil.close(err);
        }
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

    //
    // Output Helpers
    //

    public void debug(final String msg) {
        if (isDebug()) {
            out.println(msg);
        }
    }

    public void debug(final String format, final Object... args) {
        if (isDebug()) {
            out.println(MessageFormatter.arrayFormat(format, args));
        }
    }

    public void verbose(final String msg) {
        if (isVerbose()) {
            out.println(msg);
        }
    }

    public void verbose(final String format, final Object... args) {
        if (isVerbose()) {
            out.println(MessageFormatter.arrayFormat(format, args));
        }
    }
    
    public void info(final String msg) {
        if (!isQuiet()) {
            out.println(msg);
        }
    }

    public void info(final String format, final Object... args) {
        if (!isQuiet()) {
            out.println(MessageFormatter.arrayFormat(format, args));
        }
    }

    public void warn(final String msg) {
        if (!isQuiet()) {
            err.println(msg);
        }
    }

    public void warn(final String format, final Object... args) {
        if (!isQuiet()) {
            err.println(MessageFormatter.arrayFormat(format, args));
        }
    }

    public void error(final String msg) {
        err.println(msg);
    }

    public void error(final String format, final Object... args) {
        err.println(MessageFormatter.arrayFormat(format, args));
    }
}