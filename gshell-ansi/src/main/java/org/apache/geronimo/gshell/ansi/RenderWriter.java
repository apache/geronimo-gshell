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

package org.apache.geronimo.gshell.ansi;

import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Print writer implementation which supports automatic ANSI color rendering
 *
 * @version $Rev$ $Date$
 */
public class RenderWriter
    extends PrintWriter
{
    private final Renderer renderer = new Renderer();

    public RenderWriter(final OutputStream out) {
        super(out);
    }

    public RenderWriter(final OutputStream out, final boolean autoFlush) {
        super(out, autoFlush);
    }

    public RenderWriter(final Writer out) {
        super(out);
    }

    public RenderWriter(final Writer out, final boolean autoFlush) {
        super(out, autoFlush);
    }

    public void write(final String s) {
        if (Renderer.test(s)) {
            super.write(renderer.render(s));
        }
        else {
            super.write(s);
        }
    }
}
