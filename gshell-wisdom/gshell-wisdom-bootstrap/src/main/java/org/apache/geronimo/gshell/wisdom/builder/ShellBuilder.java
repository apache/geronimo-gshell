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

package org.apache.geronimo.gshell.wisdom.builder;

import org.apache.geronimo.gshell.application.ApplicationManager;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.model.application.ApplicationModel;
import org.apache.geronimo.gshell.shell.ShellFactory;

/**
 * Builds {@link org.apache.geronimo.gshell.shell.Shell} instanes.
 *
 * @version $Rev$ $Date$
 */
public interface ShellBuilder
    extends ShellFactory
{
    ClassLoader getClassLoader();

    void setClassLoader(ClassLoader classLoader);

    IO getIo();

    void setIo(IO io);

    Variables getVariables();

    void setVariables(Variables variables);

    ApplicationModel getApplicationModel();

    void setApplicationModel(ApplicationModel applicationModel);

    ApplicationManager getApplicationManager();

    void setApplicationManager(ApplicationManager applicationManager);
}