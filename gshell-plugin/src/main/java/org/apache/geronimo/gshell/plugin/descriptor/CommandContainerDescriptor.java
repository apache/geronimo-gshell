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

package org.apache.geronimo.gshell.plugin.descriptor;

import org.apache.geronimo.gshell.command.CommandContainer;
import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.model.command.Command;
import org.apache.geronimo.gshell.plugin.DefaultCommandContainer;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class CommandContainerDescriptor
    extends ComponentDescriptorSupport
{
    private final Command command;

    public CommandContainerDescriptor(final Command command) {
        assert command != null;

        this.command = command;

        setRole(CommandContainer.class);
        setRoleHint(command.getId());
        setImplementation(DefaultCommandContainer.class);
        setVersion(command.getVersion());
        setIsolatedRealm(false);
        setInstantiationStrategy("singleton");

        XmlPlexusConfiguration config = new XmlPlexusConfiguration("configuration");
        config.addChild(new XmlPlexusConfiguration("commandId", command.getId()));
        setConfiguration(config);
    }

    public Command getCommand() {
        return command;
    }
    
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}