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

package org.apache.geronimo.gshell.commands.remote;

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.io.PromptReader;
import org.apache.geronimo.gshell.notification.ExitNotification;
import org.apache.geronimo.gshell.remote.client.proxy.RemoteShellProxy;
import org.apache.geronimo.gshell.remote.client.RshClient;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.spring.BeanContainerAware;
import org.apache.geronimo.gshell.i18n.MessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Connect to a remote shell server.
 *
 * @version $Rev$ $Date$
 */
public class RshAction
    implements CommandAction, BeanContainerAware
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Option(name="-b", aliases={"--bind"})
    private URI local;

    @Option(name="-u", aliases={"--username"})
    private String username;

    @Option(name="-p", aliases={"--password"})
    private String password;
    
    @Argument(required=true, index=0)
    private URI remote;

    @Argument(index=1, multiValued=true)
    private List<String> command = null;

    private BeanContainer container;

    public void setBeanContainer(final BeanContainer container) {
        assert container != null;
        this.container = container;
    }

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();
        MessageSource messages = context.getCommand().getMessages();

        io.info(messages.format("info.connecting", remote));

        RshClient client = container.getBean(RshClient.class);

        log.debug("Created client: {}", client);
        
        client.connect(remote, local);

        io.info(messages.getMessage("info.connected"));

        // If the username/password was not configured via cli, then prompt the user for the values
        if (username == null || password == null) {
            PromptReader prompter = new PromptReader(io);

            if (username == null) {
                username = prompter.readLine(messages.getMessage("prompt.username") + ": ");
            }

            if (password == null) {
                password = prompter.readPassword(messages.getMessage("prompt.password") + ": ");
            }

            //
            // TODO: Handle null inputs... Maybe add this support to PromptReader, then after n tries throw an exception?
            //
        }

        client.login(username, password);

        // client.echo("HELLO");
        // Thread.sleep(1 * 1000);

        RemoteShellProxy shell = new RemoteShellProxy(client, io);

        Object result = Result.SUCCESS;

        try {
            if (command == null) {
                command = new ArrayList<String>();
            }
            
            shell.run(command.toArray());
        }
        catch (ExitNotification n) {
            // Make sure that we catch this notification, so that our parent shell doesn't exit when the remote shell does
            result = n.code;
        }

        shell.close();
        
        io.verbose(messages.getMessage("verbose.disconnecting"));

        client.close();

        io.verbose(messages.getMessage("verbose.disconnected"));

        return result;
    }
}