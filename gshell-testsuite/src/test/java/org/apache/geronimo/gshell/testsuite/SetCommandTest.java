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

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class SetCommandTest
    extends TestCase
{
    public void testFIXME() {}

    // FIXME: Need to use plexus ...
    /*
    public void testSimple() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IO io = new IO(System.in, out);

        Shell shell = new Shell(io);
        shell.execute("set", "a=b");

        assertEquals("b", shell.getVariables().get("a"));
    }
    */
}
