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

import org.apache.geronimo.gshell.DefaultEnvironment;
import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.model.application.Application;
import org.apache.geronimo.gshell.shell.Environment;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class ApplicationConfiguration
{
    private IO io;

    private Environment environment;

    private Application application;

    private IO createIo() {
        return new IO();
    }

    public IO getIo() {
        if (io == null) {
            io = createIo();
        }
        return io;
    }

    public void setIo(final IO io) {
        this.io = io;
    }

    private Environment createEnvironment() {
        return new DefaultEnvironment(getIo());
    }

    public Environment getEnvironment() {
        if (environment == null) {
            environment = createEnvironment();
        }

        return environment;
    }

    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(final Application application) {
        this.application = application;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}