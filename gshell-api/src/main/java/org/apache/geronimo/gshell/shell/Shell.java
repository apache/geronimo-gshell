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

package org.apache.geronimo.gshell.shell;

/**
 * Provides access to execute commands.
 *
 * @version $Rev$ $Date$
 */
public interface Shell
{
    ShellContext getContext();

    Object execute(String line) throws Exception;

    Object execute(String command, Object[] args) throws Exception;

    Object execute(Object... args) throws Exception;

    boolean isOpened();

    void close();

    /**
     * Check if the shell can be run interactivly.
     * 
     * @return  True if the shell is interactive.
     */
    boolean isInteractive();

    /**
     * Run the shell interactivly.
     *
     * @param args  The initial commands to execute interactivly.
     *
     * @throws Exception                        Failed to execute commands.
     * @throws UnsupportedOperationException    The shell does not support interactive execution.
     */
    void run(Object... args) throws Exception;
}
