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

package org.apache.geronimo.gshell.application;

import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.yarn.ReflectionToStringBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Default {@link Variables} implementation.
 *
 * @version $Rev$ $Date$
 */
public class DefaultVariables
    implements Variables
{
    private final Map<String,Object> map;

    private final Variables parent;

    private final Set<String> immutables = new HashSet<String>();

    public DefaultVariables(final Map<String,Object> map, final Variables parent) {
        assert map != null;
        assert parent != null;

        this.map = map;
        this.parent = parent;
    }

    public DefaultVariables(final Variables parent) {
        assert parent != null;

        this.map = new HashMap<String,Object>();
        this.parent = parent;
    }

    public DefaultVariables(final Map<String,Object> map) {
        assert map != null;

        this.map = map;
        this.parent = null;
    }

    public DefaultVariables() {
        this(new HashMap<String,Object>());
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
    
    public void set(final String name, final Object value) {
        set(name, value, true);
    }

    public void set(final String name, final Object value, boolean mutable) {
        assert name != null;

        ensureMutable(name);

        map.put(name, value);

        if (!mutable) {
            immutables.add(name);
        }
    }

    public Object get(final String name) {
        assert name != null;

        Object value = map.get(name);
        if (value == null && parent != null) {
            value = parent.get(name);
        }

        return value;
    }

    public Object get(final String name, final Object _default) {
        Object value = get(name);
        if (value == null) {
            return _default;
        }

        return value;
    }

    public void unset(final String name) {
        assert name != null;

        ensureMutable(name);

        map.remove(name);
    }

    public boolean contains(final String name) {
        assert name != null;

        return map.containsKey(name);
    }

    public boolean isMutable(final String name) {
        assert name != null;

        boolean mutable = true;

        // First ask out parent if there is one, if they are immutable, then so are we
        if (parent != null) {
            mutable = parent.isMutable(name);
        }

        if (mutable) {
            mutable = !immutables.contains(name);
        }

        return mutable;
    }

    private void ensureMutable(final String name) {
        assert name != null;

        if (!isMutable(name)) {
            throw new ImmutableVariableException(name);
        }
    }

    public boolean isCloaked(final String name) {
        assert name != null;

        int count = 0;

        Variables vars = this;
        while (vars != null && count < 2) {
            if (vars.contains(name)) {
                count++;
            }

            vars = vars.parent();
        }

        return count > 1;
    }

    public Iterator<String> names() {
        // Chain to parent iterator if we have a parent
        return new Iterator<String>() {
            Iterator<String> iter = map.keySet().iterator();
            boolean more = parent() != null;

            public boolean hasNext() {
                boolean next = iter.hasNext();
                if (!next && more) {
                    iter = parent().names();
                    more = false;
                    next = hasNext();
                }

                return next;
            }

            public String next() {
                return iter.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public Variables parent() {
        return parent;
    }

    //
    // FIXME: Move to some other class...
    //

    public static boolean isIdentifier(final String name) {
        if (name == null || name.length() == 0) {
            return false;
        }

        char[] chars = name.toCharArray();

        if (!Character.isJavaIdentifierStart(chars[0])) {
            return false;
        }

        /*
        FIXME: This fails for stuff like 'gshell.prompt' which we should allow
               Eventually need to get this fixed, for now just skip part checking

        for (int i=1; i<chars.length; i++) {
            if (!Character.isJavaIdentifierPart(chars[i])) {
                return false;
            }
        }
        */

        return true;
    }
}
