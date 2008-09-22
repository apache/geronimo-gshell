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

package org.apache.geronimo.gshell.wisdom.plugin.bundle;

import org.apache.geronimo.gshell.command.Command;

import java.util.List;

/**
 * A bundle of {@link Command} instances.
 *
 * @version $Rev$ $Date$
 */
public class CommandBundle
    implements Bundle
{
    private String id;

    private List<Command> commands;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        assert id != null;
        
        this.id = id;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public void setCommands(final List<Command> commands) {
        assert commands != null;

        this.commands = commands;
    }

    public int size() {
        return commands != null ? commands.size() : 0;
    }
    
    public boolean isEmpty() {
        return size() == 0;
    }
}