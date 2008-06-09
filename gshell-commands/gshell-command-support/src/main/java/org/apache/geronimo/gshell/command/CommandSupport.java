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

import org.apache.geronimo.gshell.io.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides support for {@link Command} implemenations.
 *
 * @version $Rev$ $Date$
 */
public abstract class CommandSupport
    implements Executable
{
    protected Logger log = LoggerFactory.getLogger(getClass());

    protected IO io;

    protected Variables variables;

    public void init(final CommandContext context) {
        assert context != null;

        this.io = context.getIo();
        this.variables = context.getVariables();

        // Re-setup logging using our id
        String id = context.getInfo().getId();
        log = LoggerFactory.getLogger(getClass().getName() + "." + id);
    }

    public Object execute(final CommandContext context, final Object... args) throws Exception {
        assert context != null;
        assert args != null;

        init(context);
        
        return doExecute();
    }

    protected abstract Object doExecute() throws Exception;
}
