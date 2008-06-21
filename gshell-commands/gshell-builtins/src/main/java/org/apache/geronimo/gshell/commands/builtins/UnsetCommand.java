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

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Unset a variable or property.
 *
 * @version $Rev$ $Date$
 */
@CommandComponent(id="gshell-builtins:unset", description="Unset a variable")
public class UnsetCommand
    implements CommandAction
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    enum Mode
    {
        VARIABLE,
        PROPERTY
    }

    @Option(name="-m", aliases={"--mode"}, description="Unset mode")
    private Mode mode = Mode.VARIABLE;

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    @Argument(required=true, description="Variable name")
    private List<String> args;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;

        Variables variables = context.getVariables();

        for (String arg : args) {
            String namevalue = String.valueOf(arg);

            switch (mode) {
                case PROPERTY:
                    unsetProperty(namevalue);
                    break;

                case VARIABLE:
                    unsetVariable(variables, namevalue);
                    break;
            }
        }

        return Result.SUCCESS;
    }

    private void unsetProperty(final String name) {
        log.info("Unsetting system property: {}", name);

        System.getProperties().remove(name);
    }

    private void unsetVariable(final Variables vars, final String name) {
        log.info("Unsetting variable: {}", name);

        // Command vars always has a parent, set only makes sence when setting in parent's scope
        vars.parent().unset(name);
    }
}
