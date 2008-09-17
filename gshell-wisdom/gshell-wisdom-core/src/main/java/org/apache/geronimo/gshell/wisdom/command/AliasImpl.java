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

package org.apache.geronimo.gshell.wisdom.command;

import org.apache.geronimo.gshell.command.CommandResult;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.io.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class AliasImpl
    extends CommandSupport
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @PostConstruct
    private void init() {
        // TODO: setup action, documenter, completer, messages
    }

    public CommandResult execute(final Object[] args, final IO io, final Variables variables) {
        return null;
    }
}