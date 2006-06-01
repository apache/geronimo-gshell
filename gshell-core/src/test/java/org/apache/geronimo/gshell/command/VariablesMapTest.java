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

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for the {@link VariablesMap} class.
 *
 * @version $Id$
 */
public class VariablesMapTest
    extends TestCase
{
    public void testConstructorArgsNull() throws Exception {
        try {
            new VariablesMap((Map<String,Object>)null);
            fail("Accepted a null value");
        }
        catch (IllegalArgumentException expected) {
            // ignore
        }

        try {
            new VariablesMap((Variables)null);
            fail("Accepted a null value");
        }
        catch (IllegalArgumentException expected) {
            // ignore
        }

        try {
            new VariablesMap(new HashMap<String,Object>(), null);
            fail("Accepted a null value");
        }
        catch (IllegalArgumentException expected) {
            // ignore
        }

        // Happy day
        new VariablesMap(new HashMap<String,Object>(), new VariablesMap());
    }

    /*
    public void testSetNameIsNull() throws Exception {
        try {
            new VariablesMap().set(null, null);
            fail("Accepted a null value");
        }
        catch (IllegalArgumentException expected) {
            // ignore
        }
    }
    */

    public void testSet() throws Exception {
        VariablesMap vars = new VariablesMap();
        String name = "a";
        Object value = new Object();

        assertFalse(vars.contains(name));
        vars.set(name, value);
        assertTrue(vars.contains(name));

        Object obj = vars.get(name);
        assertEquals(value, obj);

        String str = vars.names().next();
        assertEquals(name, str);
    }

    public void testSetAsImmutable() throws Exception {
        VariablesMap vars = new VariablesMap();
        String name = "a";
        Object value = new Object();

        assertTrue(vars.isMutable(name));
        vars.set(name, value, false);
        assertFalse(vars.isMutable(name));

        try {
            vars.set(name, value);
            fail("Set an immutable variable");
        }
        catch (Variables.ImmutableVariableException expected) {
            // ignore
        }
    }

    public void testSetAsImmutableInParent() throws Exception {
        Variables parent = new VariablesMap();
        VariablesMap vars = new VariablesMap(parent);
        String name = "a";
        Object value = new Object();

        parent.set(name, value, false);
        assertFalse(parent.isMutable(name));
        assertFalse(vars.isMutable(name));

        try {
            vars.set(name, value);
            fail("Set an immutable variable");
        }
        catch (Variables.ImmutableVariableException expected) {
            // ignore
        }
    }

    public void testGet() throws Exception {
        VariablesMap vars = new VariablesMap();
        String name = "a";
        Object value = new Object();

        Object obj1 = vars.get(name);
        assertNull(obj1);

        vars.set(name, value);
        Object obj2 = vars.get(name);
        assertSame(value, obj2);
    }

    public void testGetUsingDefault() throws Exception {
        VariablesMap vars = new VariablesMap();
        String name = "a";
        Object value = new Object();

        Object obj1 = vars.get(name);
        assertNull(obj1);

        Object obj2 = vars.get(name, value);
        assertSame(value, obj2);
    }

    public void testGetCloaked() throws Exception {
        Variables parent = new VariablesMap();
        VariablesMap vars = new VariablesMap(parent);
        String name = "a";
        Object value = new Object();

        parent.set(name, value);
        Object obj1 = vars.get(name);
        assertEquals(value, obj1);

        Object value2 = new Object();
        vars.set(name, value2);

        Object obj2 = vars.get(name);
        assertSame(value2, obj2);
        assertNotSame(value, obj2);
    }

    public void testUnsetAsImmutable() throws Exception {
        VariablesMap vars = new VariablesMap();
        String name = "a";
        Object value = new Object();

        assertTrue(vars.isMutable(name));
        vars.set(name, value, false);
        assertFalse(vars.isMutable(name));

        try {
            vars.unset(name);
            fail("Unset an immutable variable");
        }
        catch (Variables.ImmutableVariableException expected) {
            // ignore
        }
    }

    public void testUnsetAsImmutableInParent() throws Exception {
        Variables parent = new VariablesMap();
        VariablesMap vars = new VariablesMap(parent);
        String name = "a";
        Object value = new Object();

        parent.set(name, value, false);
        assertFalse(parent.isMutable(name));
        assertFalse(vars.isMutable(name));

        try {
            vars.unset(name);
            fail("Unset an immutable variable");
        }
        catch (Variables.ImmutableVariableException expected) {
            // ignore
        }
    }

    public void testCloaking() throws Exception {
        Variables parent = new VariablesMap();
        VariablesMap vars = new VariablesMap(parent);
        String name = "a";
        Object value = new Object();

        parent.set(name, value);
        assertFalse(parent.isCloaked(name));
        assertFalse(vars.isCloaked(name));

        vars.set(name, new Object());
        assertTrue(vars.isCloaked(name));
    }

    public void testParent() throws Exception {
        Variables parent = new VariablesMap();
        assertNull(parent.parent());

        VariablesMap vars = new VariablesMap(parent);
        assertNotNull(vars.parent());

        assertEquals(parent, vars.parent());
    }
}
