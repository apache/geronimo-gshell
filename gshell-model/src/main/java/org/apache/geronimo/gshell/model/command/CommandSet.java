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

package org.apache.geronimo.gshell.model.command;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a set of commands.
 *
 * @version $Rev$ $Date$
 */
// FIXME: Use consistent case for XML tags
@XStreamAlias("command-set")
public class CommandSet
{
    private String id;

    private String description;

    //
    // FIXME: Make collection accessors null-safe
    //

    private List<Command> commands;

    public CommandSet() {}

    public CommandSet(final String id) {
        this.id = id;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public void setCommands(final List<Command> commands) {
        this.commands = commands;
    }

    public void addCommand(final Command command) {
        assert command != null;

        if (commands == null) {
            commands = new ArrayList<Command>();
        }

        commands.add(command);
    }

    public int size() {
        List<Command> list = getCommands();

        if (list != null) {
            return list.size();
        }

        return 0;
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}