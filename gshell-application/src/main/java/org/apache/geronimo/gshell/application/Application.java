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

import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.model.application.ApplicationModel;
import org.apache.geronimo.gshell.command.Variables;

import java.io.File;
import java.net.InetAddress;

/**
 * Encapsulates the context for an application.
 *
 * @version $Rev$ $Date$
 */
public interface Application
{
    String getId();
    
    IO getIo();

    Variables getVariables();
    
    ApplicationModel getModel();

    /**
     * Returns the home directory of the shell.
     *
     * @return  Shell home directory; never null;
     */
    File getHomeDir();

    /**
     * Returns the local IP address of the shell.
     *
     * @return  The local IP address of the shell; never null;
     */
    InetAddress getLocalHost();

    /**
     * Returns the name of the current user.
     *
     * @return  The current user name; never null;
     */
    String getUserName();
}