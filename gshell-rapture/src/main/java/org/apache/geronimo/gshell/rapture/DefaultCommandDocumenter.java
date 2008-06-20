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

package org.apache.geronimo.gshell.rapture;

import org.apache.geronimo.gshell.command.CommandDocumenter;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

/**
 * The default {@link org.apache.geronimo.gshell.command.CommandDocumenter} component.
 *
 * @version $Rev$ $Date$
 */
@Component(role= CommandDocumenter.class)
public class DefaultCommandDocumenter
    implements CommandDocumenter
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Configuration("") // Just to mark what this is used for, since we have to configure a default value
    private String commandId;

    public String getName() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public void renderUsage(final PrintWriter out) {
        assert out != null;

        // TODO:
    }

    public void renderManual(final PrintWriter out) {
        assert out != null;

        // TODO:
    }
}