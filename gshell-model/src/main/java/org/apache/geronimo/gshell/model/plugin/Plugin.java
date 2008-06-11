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

package org.apache.geronimo.gshell.model.plugin;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.geronimo.gshell.model.command.CommandModel;
import org.apache.geronimo.gshell.model.common.DescriptorSupport;
import org.apache.geronimo.gshell.model.layout.Layout;

import java.util.ArrayList;
import java.util.List;

/**
 * Plugin model root element.
 *
 * @version $Rev$ $Date$
 */
@XStreamAlias("plugin")
public class Plugin
    extends DescriptorSupport
{
    private List<CommandModel> commands;

    private Layout layout;

    public List<CommandModel> getCommands() {
        if (commands == null) {
            commands = new ArrayList<CommandModel>();
        }
        
        return commands;
    }

    public void add(final CommandModel model) {
        assert model != null;
        
        getCommands().add(model);
    }

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(final Layout layout) {
        this.layout = layout;
    }
}
