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

package org.apache.geronimo.gshell.testsuite;

import junit.framework.TestCase;
import org.apache.geronimo.gshell.Shell;
import org.apache.geronimo.gshell.console.IO;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class EchoCommandTest
    extends TestCase
{
    public void testSimple() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IO io = new IO(System.in, out);

        Shell shell = new Shell(io);
        shell.execute("echo", "1");

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String line1 = reader.readLine();
        assertNotNull(line1);
        assertEquals("1", line1);
    }
}
