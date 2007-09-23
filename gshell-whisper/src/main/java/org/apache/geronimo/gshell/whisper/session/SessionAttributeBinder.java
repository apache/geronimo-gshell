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

package org.apache.geronimo.gshell.whisper.session;

import org.apache.mina.common.IoSession;

/**
 * Helper to manage binding operations for typed session attribute objects.
 *
 * @version $Rev$ $Date$
 */
public class SessionAttributeBinder<T>
{
    private final String key;

    public SessionAttributeBinder(final String key) {
        assert key != null;
        
        this.key = key;
    }

    public SessionAttributeBinder(final Class type) {
        this(type.getName());
    }

    public SessionAttributeBinder(final Class type, final String suffix) {
        this(type.getName() + "." + suffix);
    }

    public boolean isBound(final IoSession session) {
        assert session != null;
        
        return session.containsAttribute(key);
    }

    @SuppressWarnings({"unchecked"})
    public T lookup(final IoSession session) {
        assert session != null;

        T obj = (T) session.getAttribute(key);

        if (obj == null) {
            throw new NotBoundException(key);
        }

        return obj;
    }

    @SuppressWarnings({"unchecked"})
    public T lookup(final IoSession session, final T defaultValue) {
        assert session != null;

        T obj = (T) session.getAttribute(key);

        if (obj == null) {
            return defaultValue;
        }

        return obj;
    }

    public T bind(final IoSession session, final T obj) {
        assert session != null;
        assert obj != null;
        
        Object prev = session.getAttribute(key);

        if (prev != null) {
            throw new AlreadyBoundException(key);
        }

        session.setAttribute(key, obj);

        return obj;
    }
    
    @SuppressWarnings({"unchecked"})
    public T rebind(final IoSession session, final T obj) {
        assert session != null;
        assert obj != null;

        T prev = (T) session.getAttribute(key);

        session.setAttribute(key, obj);

        return prev;
    }

    @SuppressWarnings({"unchecked"})
    public T unbind(final IoSession session) {
        assert session != null;

        return (T) session.removeAttribute(key);
    }

    public static class NotBoundException
        extends RuntimeException
    {
        public NotBoundException(final String key) {
            super(key);
        }
    }

    public static class AlreadyBoundException
        extends RuntimeException
    {
        public AlreadyBoundException(final String key) {
            super(key);
        }
    }
}