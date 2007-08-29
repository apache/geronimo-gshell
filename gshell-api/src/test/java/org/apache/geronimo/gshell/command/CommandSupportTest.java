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

import junit.framework.TestCase;

/**
 * Unit tests for the {@link CommandSupport} class.
 *
 * @version $Rev$ $Date$
 */
public class CommandSupportTest
    extends TestCase
{
    public void testConstructorNameIsNull() throws Exception {
        try {
            new MockCommand(null);
            fail("Accepted null value");
        }
        catch (AssertionError expected) {
            // ignore
        }
    }

    public void testSetNameIsNull() throws Exception {
        try {
            MockCommand cmd = new MockCommand();
            cmd.setName(null);
            fail("Accepted null value");
        }
        catch (AssertionError expected) {
            // ignore
        }
    }

    public void testGetNameNotSet() throws Exception {
        try {
            MockCommand cmd = new MockCommand();
            cmd.getName();
            fail("Get returned when name was unset");
        }
        catch (IllegalStateException expected) {
            // ignore
        }
    }

    //
    // MockCommand
    //

    private static class MockCommand
        extends CommandSupport
    {
        public MockCommand() {}

        public MockCommand(final String name) {
            super(name);
        }

        protected Object doExecute() throws Exception {
            return null;
        }
    }
}
