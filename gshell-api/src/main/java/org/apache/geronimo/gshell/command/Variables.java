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
     * @param name  The name of the variable to set.
     * @param value The value of the variable.
     * 
     * @throws ImmutableVariableException   The variable is immutable.
     */
    void set(String name, Object value) throws ImmutableVariableException;

    /**
     * Set a value of a variable, optional making the variable immutable.
     *
     * @param name      The name of the variable to set.
     * @param value     The value of the variable.
     * @param mutable   False to make the variable immutable.
     *
     * @throws ImmutableVariableException   The variable is immutable.
     */
    void set(String name, Object value, boolean mutable) throws ImmutableVariableException;

    /**
     * Get the value of a variable.
     *
     * @param name  The name of the variable to get.
     * @return      The value of the variable, or null if not set.
     */
    Object get(String name);

    /**
     * Get the value of a variable, if not set using the provided default.
     *
     * @param name          The name of the variable to get.
     * @param defaultValue  The default value of the variable to return if the variable was not set.
     * @return              The value of the named variable or <tt>defaultValue</tt> if the variable was not set.
     */
    Object get(String name, Object defaultValue);

    /**
     * Check if a variable is mutable.
     *
     * @param name  The name of the variable to query mutable status.
     * @return      True if the variable is mutable.
     */
    boolean isMutable(String name);

    /**
     * Check if a variable is cloaked.  Cloaked variables exist when a variable of the same name
     * has been set in the parent and that variable was not immutable.
     *
     * @param name  The name of the variable to query cloaked status.
     * @return      True if the variable is cloaked.
     */
    boolean isCloaked(String name);

    /**
     * Unset a variable.
     *
     * @param name  The name of the variable to unset.
     * @throws ImmutableVariableException   The variable is immutable.
     */
    void unset(String name) throws ImmutableVariableException;

    /**
     * Check for the existance of a variable.
     *
     * @param name  The name of the variable to query existance of.
     * @return      True if there is a variable of the given name.
     */
    boolean contains(String name);

    /**
     * Get all variable names.
     *
     * @return  All variable names.
     */
    Iterator<String> names();

    /**
     * Returns the parent variables container.
     *
     * @return  The parent variables container.
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
