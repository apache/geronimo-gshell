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

package org.apache.geronimo.gshell.remote.server;

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.spring.BeanContainerAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Command to start a remote shell server.
 *
 * @version $Rev$ $Date$
 */
public class RshServerCommand
    implements CommandAction, BeanContainerAware
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Option(name="-b", aliases={ "--background"})
    private boolean background;

    @Argument(token="URI", required=true)
    private URI location;

    private BeanContainer container;

    public void setBeanContainer(final BeanContainer container) {
        assert container != null;

        this.container = container;
    }

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;

        RshServer server = container.getBean(RshServer.class);

        log.debug("Created server: {}", server);

        server.bind(location);

        IO io = context.getIo();

        io.info("Listening on: {}", location);

        if (!background) {
            synchronized (this) {
                log.debug("Waiting for server to shutdown");
                
                wait();
            }

            server.close();
        }
        
        return Result.SUCCESS;
    }
}