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

import org.apache.geronimo.gshell.common.Arguments;

import java.util.Properties;

/**
 * Container for the very basic information about a command.
 *
 * @version $Rev$ $Date$
 */
public class CommandDefinition
{
    private final String name;

    private final String classname;

    private final String[] aliases;

    private final boolean enabled;

    private final String category;

    public CommandDefinition(final Properties props) throws InvalidDefinitionException {
        assert props != null;
        
        this.name = props.getProperty("name");
        if (name == null) {
            throw new MissingPropertyException("name", props);
        }

        this.classname = props.getProperty("class");
        if (classname == null) {
            throw new MissingPropertyException("class", props);
        }

        this.aliases = loadAliasesFrom(props);

        this.enabled = Boolean.getBoolean(props.getProperty("enable"));

        this.category = props.getProperty("category");
        if (category == null) {
            throw new MissingPropertyException("category", props);
        }
    }

    public String toString() {
        return getName() + "=" + getClassName() +
                "{ aliases=" + Arguments.asString(getAliases()) +
                ", enabled=" + enabled +
                ", category=" + category +
                " }";
    }

    //
    // TODO: Add 'alias' and 'unalias' command support
    //

    private String[] loadAliasesFrom(final Properties props) {
        String input = props.getProperty("aliases");

        if (input != null) {
            String[] list = input.split(",");
            for (int i=0; i < list.length; i++) {
                list[i] = list[i].trim();
            }

            return list;
        }
        else {
            return new String[0];
        }
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return classname;
    }

    public String[] getAliases() {
        return aliases;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getCategory() {
        return category;
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
        ///CLOVER:OFF

        public InvalidDefinitionException(String msg) {
            super(msg);
        }
    }

    public static class MissingPropertyException
        extends InvalidDefinitionException
    {
        ///CLOVER:OFF

        MissingPropertyException(String name, Properties props) {
            super("Missing '" + name + "' property in command definition: " + props);
        }
    }
}
