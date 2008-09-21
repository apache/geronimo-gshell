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
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.model.application.ApplicationModel;
import org.apache.geronimo.gshell.yarn.Yarn;

/**
 * Container for application configuration.
 *
 * @version $Rev$ $Date$
 */
public class ApplicationConfiguration
{
    private IO io;

    private Variables variables;

    private ApplicationModel model;

    public IO getIo() {
        return io;
    }

    public void setIo(final IO io) {
        this.io = io;
    }

    public Variables getVariables() {
        return variables;
    }

    public void setVariables(final Variables variables) {
        this.variables = variables;
    }

    public ApplicationModel getModel() {
        return model;
    }

    public void setModel(final ApplicationModel model) {
        this.model = model;
    }

    public void validate() {
        if (model == null) {
            throw new IllegalStateException("Missing application model");
        }    
    }

    public String toString() {
        return Yarn.render(this);
    }
}