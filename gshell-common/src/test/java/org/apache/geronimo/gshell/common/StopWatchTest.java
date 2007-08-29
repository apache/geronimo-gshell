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

package org.apache.geronimo.gshell.common;

import org.apache.commons.lang.time.StopWatch;

import junit.framework.TestCase;

/**
 * Simple test to ensure we pick up the required bits from <tt>commons-lang</tt> to use the {@link StopWatch} class.
 *
 * @version $Rev$ $Date$
 */
public class StopWatchTest
    extends TestCase
{
    public void test1() throws Exception {
        StopWatch watch = new StopWatch();
    }
}