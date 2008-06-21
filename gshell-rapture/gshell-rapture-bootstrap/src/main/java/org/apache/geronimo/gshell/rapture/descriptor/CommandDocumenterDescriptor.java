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

package org.apache.geronimo.gshell.rapture.descriptor;

import org.apache.geronimo.gshell.command.CommandDocumenter;
import org.apache.geronimo.gshell.model.command.CommandModel;

/**
 * Descriptor for a {@link CommandDocumenter} Plexus component.
 *
 * @version $Rev$ $Date$
 */
public class CommandDocumenterDescriptor
    extends ComponentDescriptorSupport
{
    private final CommandModel model;

    public CommandDocumenterDescriptor(final CommandModel model) {
        assert model != null;

        this.model = model;

        setRole(CommandDocumenter.class);
        setRoleHint(model.getId());
        setImplementation("org.apache.geronimo.gshell.rapture.command.DefaultCommandDocumenter");
        setVersion(model.getVersion());
        setIsolatedRealm(false);
        setInstantiationStrategy("singleton");
    }
}