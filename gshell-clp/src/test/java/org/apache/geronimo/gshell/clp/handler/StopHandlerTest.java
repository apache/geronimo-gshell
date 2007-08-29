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

package org.apache.geronimo.gshell.clp.handler;

import java.util.List;

import junit.framework.TestCase;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.CommandLineProcessor;
import org.apache.geronimo.gshell.clp.Option;

/**
 * Tests for the {@link StopHandler} class.
 *
 * @version $Rev$ $Date$
 */
public class StopHandlerTest
    extends TestCase
{
    TestBean bean;

    CommandLineProcessor clp;

    protected void setUp() throws Exception {
        bean = new TestBean();
        clp = new CommandLineProcessor(bean);

        assertEquals(2, clp.getOptionHandlers().size());
        assertEquals(1, clp.getArgumentHandlers().size());
    }

    protected void tearDown() throws Exception {
        bean = null;
        clp = null;
    }

    public void test1() throws Exception {
        clp.process("-1", "test", "--", "-foo", "bar");

        assertEquals("test", bean.s);
        assertNotNull(bean.args);
        assertEquals(2, bean.args.size());
        assertEquals("-foo", bean.args.get(0));
        assertEquals("bar", bean.args.get(1));

    }

    private static class TestBean
    {
        @Option(name="-1")
        String s;

        @Option(name="--", handler=StopHandler.class)
        boolean stop;

        @Argument
        List<String> args;
    }
}