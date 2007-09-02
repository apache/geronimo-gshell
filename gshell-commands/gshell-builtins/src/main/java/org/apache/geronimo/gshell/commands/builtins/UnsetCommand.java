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

package org.apache.geronimo.gshell.commands.builtins;

import java.util.List;

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.VariablesImpl;

/**
 * Unset a variable or property.
 *
 * @version $Rev$ $Date$
 */
public class UnsetCommand
    extends CommandSupport
{
    enum Mode
    {
        VARIABLE,
        PROPERTY
    }

    @Option(name="-m", aliases={"--mode"}, description="Unset mode")
    private Mode mode = Mode.VARIABLE;

    @Argument(required=true, description="Variable name")
    private List<String> args;

    public UnsetCommand() {
        super("unset");
    }

    protected String getUsage() {
        return super.getUsage() + " (<name>)+";
    }

    protected Object doExecute() throws Exception {
        for (String arg : args) {
            String namevalue = String.valueOf(arg);

            switch (mode) {
                case PROPERTY:
                    unsetProperty(namevalue);
                    break;

                case VARIABLE:
                    unsetVariable(namevalue);
                    break;
            }
        }

        return SUCCESS;
    }

    private void ensureIsIdentifier(final String name) {
        if (!VariablesImpl.isIdentifier(name)) {
            throw new RuntimeException("Invalid identifer name: " + name);
        }
    }

    private void unsetProperty(final String name) {
        log.info("Unsetting system property: " + name);

        ensureIsIdentifier(name);

        System.getProperties().remove(name);
    }

    private void unsetVariable(final String name) {
        log.info("Unsetting variable: " + name);

        ensureIsIdentifier(name);

        // Command vars always has a parent, set only makes sence when setting in parent's scope
        Variables vars = this.getVariables().parent();

        vars.unset(name);
    }
}
