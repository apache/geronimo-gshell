/*
 * Copyright 2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.gshell.command;

import java.util.Properties;

/**
 * Container for the very basic information about a command.
 *
 * @version $Id$
 */
public class CommandDefinition
{
    private final String name;

    private final String classname;

    public CommandDefinition(final Properties props) throws InvalidDefinitionException {
        this.name = props.getProperty("name");
        if (name == null) {
            throw new MissingPropertyException("name", props);
        }

        this.classname = props.getProperty("class");
        if (classname == null) {
            throw new MissingPropertyException("class", props);
        }
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return classname;
    }

    public String toString() {
        return getName() + "=" + getClassName();
    }

    public Class loadClass() throws ClassNotFoundException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl.loadClass(getClassName());
    }

    //
    // Exceptions
    //

    public static class InvalidDefinitionException
        extends CommandException
    {
        public InvalidDefinitionException(String msg) {
            super(msg);
        }
    }

    public static class MissingPropertyException
        extends InvalidDefinitionException
    {
        MissingPropertyException(String name, Properties props) {
            super("Missing '" + name + "' property in command definition: " + props);
        }
    }
}
