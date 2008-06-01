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
import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.shell.Environment;

/**
 * Default {@link Environment} implementation.
 *
 * @version $Rev$ $Date$
 */
public class DefaultEnvironment
    implements Environment
{
    private final IO io;

    private final Variables vars;

    public DefaultEnvironment(final IO io, final Variables vars) {
        assert io != null;
        assert vars != null;

        this.io = io;
        this.vars = vars;

        vars.set("env", System.getenv(), false);
    }

    public DefaultEnvironment(final IO io) {
        this(io, new DefaultVariables());
    }

    public IO getIO() {
        return io;
    }

    public Variables getVariables() {
        return vars;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}