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

package org.apache.geronimo.gshell.rapture.command;

import org.apache.geronimo.gshell.command.CommandInfo;
import org.apache.geronimo.gshell.model.layout.AliasNode;
import org.apache.geronimo.gshell.model.layout.CommandNode;
import org.apache.geronimo.gshell.model.layout.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default {@link CommandInfo} implementation.
 *
 * @version $Rev$ $Date$
 */
public class DefaultCommandInfo
    implements CommandInfo
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Node node;

    public DefaultCommandInfo(final Node node) {
        assert node != null;
        this.node = node;
    }

    public String getId() {
        if (node instanceof CommandNode) {
            return ((CommandNode)node).getId();
        }
        else if (node instanceof AliasNode) {
            return ((AliasNode)node).getCommand();
        }

        throw new IllegalStateException();
    }

    public String getName() {
        if (node instanceof AliasNode) {
            return ((AliasNode)node).getCommand();
        }

        return node.getName();
    }

    public String getAlias() {
        if (node instanceof AliasNode) {
            return node.getName();
        }

        return null;
    }

    public String getPath() {
        throw new Error();
    }
}