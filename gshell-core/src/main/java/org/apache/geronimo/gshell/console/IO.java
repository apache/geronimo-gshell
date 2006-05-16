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
import java.io.Reader;
import java.io.PrintWriter;

/**
 * Container for input/output handles.
 *
 * @version $Id$
 */
public class IO
{
    //
    // TODO: Expose the binary versions, wrap for text readers/writers
    //
    
    public final Reader in;
    public final PrintWriter out;
    public final PrintWriter err;
    
    public IO(final Reader in, final PrintWriter out, final PrintWriter err) {
        assert in != null;
        assert out != null;
        assert err != null;
        
        this.in = in;
        this.out = out;
        this.err = err;
    }
    
    public IO(final InputStream in, final PrintStream out, final PrintStream err) {
        this(new InputStreamReader(in), new PrintWriter(out), new PrintWriter(err));
    }
    
    public IO() {
        this(System.in, System.out, System.err);
    }
    
    public void flush() {
        out.flush();
        err.flush();
    }
}
