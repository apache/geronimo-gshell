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

import java.util.Iterator;

/**
 * Provides command instances with nested namespace for storing context.
 *
 * @version $Rev$ $Date$
 */
public interface Variables
{
    /**
     * Set a value of a variable.
     *
     * @param name
     * @param value
     * @throws ImmutableVariableException
     */
    void set(String name, Object value) throws ImmutableVariableException;

    /**
     * Set a value of a variable, optional making the variable immutable.
     *
     * @param name
     * @param value
     * @param mutable
     * @throws ImmutableVariableException
     */
    void set(String name, Object value, boolean mutable) throws ImmutableVariableException;

    /**
     * Get the value of a variable.
     *
     * @param name
     * @return
     */
    Object get(String name);

    /**
     * Get the value of a variable, if not set using the provided default.
     *
     * @param name
     * @param _default
     * @return
     */
    Object get(String name, Object _default);

    /**
     * Check if a variable is mutable.
     *
     * @param name
     * @return
     */
    boolean isMutable(String name);

    /**
     * Check if a variable is cloaked.  Cloaked variables exist when a variable of the same name
     * has been set in the parent and that variable was not immutable.
     *
     * @param name
     * @return
     */
    boolean isCloaked(String name);

    /**
     * Unset a variable.
     *
     * @param name
     * @throws ImmutableVariableException
     */
    void unset(String name) throws ImmutableVariableException;

    /**
     * Check for the existance of a variable.
     *
     * @param name
     * @return
     */
    boolean contains(String name);

    /**
     * Get all variable names.
     *
     * @return
     */
    Iterator<String> names();

    /**
     * Returns the parent variables container.
     *
     * @return
     */
    Variables parent();

    //
    // Exceptions
    //

    /**
     * Throw to indicate that a variable change was attempted but the variable was not muable.
     */
    class ImmutableVariableException
        extends RuntimeException
    {
        ///CLOVER:OFF
        
        public ImmutableVariableException(final String name) {
            super("Variable is immutable: " + name);
        }
    }
}
