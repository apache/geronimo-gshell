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

package org.apache.geronimo.gshell.command;

import junit.framework.TestCase;

/**
 * Unit tests for the {@link MessageSourceImpl} class.
 *
 * @version $Id$
 */
public class MessageSourceImplTest
    extends TestCase
{
    public void testLoadAndGetMessage() throws Exception {
        MessageSourceImpl messages = new MessageSourceImpl(getClass().getName());

        String a = messages.getMessage("a");
        assertEquals("1", a);

        String b = messages.getMessage("b");
        assertEquals("2", b);

        String c = messages.getMessage("c");
        assertEquals("3", c);

        String f = messages.getMessage("f", a, b, c);
        assertEquals("1 2 3", f);
    }
}
