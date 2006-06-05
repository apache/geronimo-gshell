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

import java.util.Iterator;

/**
 * ???
 *
 * @version $Id$
 */
public interface Variables
{
    void set(String name, Object value) throws ImmutableVariableException;

    void set(String name, Object value, boolean mutable) throws ImmutableVariableException;

    Object get(String name);

    Object get(String name, Object _default);

    boolean isMutable(String name);

    boolean isCloaked(String name);

    void unset(String name) throws ImmutableVariableException;
    
    boolean contains(String name);
    
    Iterator<String> names();

    Variables parent();

    //
    // Exceptions
    //

    class ImmutableVariableException
        extends RuntimeException
    {
        ///CLOVER:OFF
        
        public ImmutableVariableException(final String name) {
            super("Variable is immutable: " + name);
        }
    }
}
