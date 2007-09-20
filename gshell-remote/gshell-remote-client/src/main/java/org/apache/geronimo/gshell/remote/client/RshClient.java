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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.PublicKey;

import org.apache.geronimo.gshell.remote.crypto.CryptoContext;
import org.apache.geronimo.gshell.remote.message.CloseShellMessage;
import org.apache.geronimo.gshell.remote.message.EchoMessage;
import org.apache.geronimo.gshell.remote.message.ExecuteMessage;
import org.apache.geronimo.gshell.remote.message.HandShakeMessage;
import org.apache.geronimo.gshell.remote.message.LoginMessage;
import org.apache.geronimo.gshell.remote.message.Message;
import org.apache.geronimo.gshell.remote.message.OpenShellMessage;
import org.apache.geronimo.gshell.remote.transport.Transport;
import org.apache.geronimo.gshell.remote.transport.TransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides support for the client-side of the remote shell protocol.
 *
 * @version $Rev$ $Date$
 */
public class RshClient
{
    //
    // TODO: If/when we want to add a state-machine to keep things in order, add client-side here.
    //
    
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final CryptoContext crypto;

    private final Transport transport;

    public RshClient(final CryptoContext crypto, final URI remote, final URI local, final TransportFactory factory) throws Exception {
        assert crypto != null;
        assert remote != null;
        // local may be null
        assert factory != null;

        this.crypto = crypto;

        // And then lets connect to the remote server
        this.transport = factory.connect(remote, local);
        
        log.debug("Connected to: {}", remote);
    }

    public void login(final String username, final String password) throws Exception {
        assert username != null;
        assert password != null;

        log.debug("Starting handshake", username);

        HandShakeMessage.Result handShakeResult = (HandShakeMessage.Result) transport.request(new HandShakeMessage(crypto.getPublicKey()));
        
        PublicKey serverKey = handShakeResult.getPublicKey();

        log.debug("Logging in: {}", username);

        LoginMessage.Result loginResult = (LoginMessage.Result) transport.request(new LoginMessage(serverKey, username, password));

        log.debug("Login Result: {}", loginResult);
    }
    
    public void echo(final String text) throws Exception {
        log.debug("Echoing: {}", text);

        transport.send(new EchoMessage(text));
    }

    public void openShell() throws Exception {
        log.debug("Opening remote shell");

        Message resp = transport.request(new OpenShellMessage());

        //
        // TODO: Need some context from the response
        //

        // log.debug("Response: {}", resp);
    }

    public void closeShell() throws Exception {
        log.debug("Closing remote shell");

        Message resp = transport.request(new CloseShellMessage());

        //
        // TODO: Need some context from the response
        //
        
        // log.debug("Response: {}", resp);
    }

    private Object doExecute(final ExecuteMessage msg) throws Exception {
        assert msg != null;

        ExecuteMessage.Result result = (ExecuteMessage.Result) transport.request(msg);

        // Handle result notifications
        if (result instanceof ExecuteMessage.Notification) {
            ExecuteMessage.Notification n = (ExecuteMessage.Notification)result;

            throw n.getNotification();
        }

        // Handle result faults
        if (result instanceof ExecuteMessage.Fault) {
            ExecuteMessage.Fault fault = (ExecuteMessage.Fault)result;

            throw new RemoteExecuteException(fault.getCause());
        }

        Object rv = result.getResult();

        log.debug("Command result: {}", rv);

        return rv;
    }

    public Object execute(final String line) throws Exception {
        assert line != null;

        return doExecute(new ExecuteMessage(line));
    }

    public Object execute(final Object... args) throws Exception {
        assert args != null;

        return doExecute(new ExecuteMessage(args));
    }

    public Object execute(final String path, final Object[] args) throws Exception {
        assert path != null;
        assert args != null;

        return doExecute(new ExecuteMessage(path, args));
    }

    public InputStream getInputStream() {
        return transport.getInputStream();
    }

    public OutputStream getOutputStream() {
        return transport.getOutputStream();
    }

    public void close() {
        transport.close();

        log.debug("Closed");
    }
}