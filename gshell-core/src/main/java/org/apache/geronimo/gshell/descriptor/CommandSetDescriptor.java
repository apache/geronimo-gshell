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

package org.apache.geronimo.gshell.descriptor;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.Annotations;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;

/**
 * Describes a set of commands.
 *
 * @version $Rev$ $Date$
 */
@XStreamAlias("command-set")
public class CommandSetDescriptor
{
    private String id;

    private String description;

    private List<CommandDescriptor> commands;

    public CommandSetDescriptor(final String id) {
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

    public List<CommandDescriptor> getCommands() {
        return commands;
    }

    public void setCommands(final List<CommandDescriptor> commands) {
        this.commands = commands;
    }

    public void addCommand(final CommandDescriptor command) {
        assert command != null;

        if (commands == null) {
            commands = new ArrayList<CommandDescriptor>();
        }

        commands.add(command);
    }

    public int size() {
        List<CommandDescriptor> list = getCommands();

        if (list != null) {
            return list.size();
        }

        return 0;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    //
    // XML Conversion
    //

    private static XStream createXStream() {
        XStream xs = new XStream(new DomDriver());

        Annotations.configureAliases(xs,
                CommandSetDescriptor.class,
                CommandDescriptor.class,
                CommandRequirement.class,
                CommandConfiguration.class,
                CommandDependency.class);

        return xs;
    }

    public static CommandSetDescriptor fromXML(final Reader input) {
        assert input != null;

        return (CommandSetDescriptor) createXStream().fromXML(input);
    }

    public static String toXML(final CommandSetDescriptor commands) {
        assert commands != null;

        return createXStream().toXML(commands);
    }

    public static void toXML(final CommandSetDescriptor commands, final Writer writer) {
        assert commands != null;
        assert writer != null;

        createXStream().toXML(commands, writer);
    }
}