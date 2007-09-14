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

package org.apache.geronimo.gshell.remote.client;

import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
@CommandComponent(id="rsh")
public class RshCommand
    extends CommandSupport
{
    private String hostname = "localhost";

    private int port = 9999;

    @Requirement
    private RshClient client;

    protected Object doExecute() throws Exception {
        client.connect(hostname, port);

        client.echo("TESTING");

        return SUCCESS;
    }
}