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
import java.util.List;

import org.apache.geronimo.gshell.remote.client.handler.ClientMessageHandler;
import org.apache.geronimo.gshell.remote.client.handler.ClientSessionContext;
import org.apache.geronimo.gshell.remote.crypto.CryptoContext;
import org.apache.geronimo.gshell.remote.message.CloseShellMessage;
import org.apache.geronimo.gshell.remote.message.ConnectMessage;
import org.apache.geronimo.gshell.remote.message.EchoMessage;
import org.apache.geronimo.gshell.remote.message.ExecuteMessage;
import org.apache.geronimo.gshell.remote.message.LoginMessage;
import org.apache.geronimo.gshell.remote.message.OpenShellMessage;
import org.apache.geronimo.gshell.whisper.message.Message;
import org.apache.geronimo.gshell.whisper.transport.Transport;
import org.apache.geronimo.gshell.whisper.transport.TransportFactory;
import org.apache.geronimo.gshell.whisper.transport.TransportFactoryLocator;
import org.apache.mina.common.IoSession;
import org.apache.mina.handler.demux.DemuxingIoHandler;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.InstantiationStrategy;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides support for the client-side of the remote shell protocol.
 *
 * @version $Rev$ $Date$
 */
@Component(role=RshClient.class, instantiationStrategy=InstantiationStrategy.PER_LOOKUP)
public class RshClient
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private PlexusContainer container;

    @Requirement
    private CryptoContext crypto;
    
    @Requirement
    private TransportFactoryLocator locator;

    private Transport transport;

    public void connect(final URI remote, final URI local) throws Exception {
        TransportFactory factory = locator.locate(remote);

        transport = factory.connect(remote, local, new Handler());

        log.debug("Connected to: {}", remote);
    }

    public InputStream getInputStream() {
        return transport.getInputStream();
    }

    public OutputStream getOutputStream() {
        return transport.getOutputStream();
    }

    public Transport getTransport() {
        return transport;
    }

    public void close() {
        transport.close();
    }

    public void login(final String username, final String password) throws Exception {
        assert username != null;
        assert password != null;

        log.debug("Starting handshake", username);

        Message response;

        response = transport.request(new ConnectMessage(crypto.getPublicKey()));

        PublicKey serverKey = ((ConnectMessage.Result)response).getPublicKey();

        log.debug("Logging in: {}", username);

        response = transport.request(new LoginMessage(username, password));

        if (response instanceof LoginMessage.Success) {
            log.debug("Login successful");
        }
        else if (response instanceof LoginMessage.Failure) {
            LoginMessage.Failure failure = (LoginMessage.Failure)response;

            throw new Exception("Login failed: " + failure.getReason());
        }
        else {
            throw new InternalError();
        }
    }
    
    public void echo(final String text) throws Exception {
        assert text != null;
        
        log.debug("Echoing: {}", text);

        transport.send(new EchoMessage(text)).join();
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

    //
    // IO Handler
    //

    private class Handler
        extends DemuxingIoHandler
    {
        public Handler() throws Exception {
            // noinspection unchecked
            List<ClientMessageHandler> handlers = (List<ClientMessageHandler>)container.lookupList(ClientMessageHandler.class);

            // Complain if we don't have any handlers
            if (handlers.isEmpty()) {
                throw new Error("No client message handlers were discovered");
            }

            for (ClientMessageHandler handler : handlers) {

                register(handler);
            }
        }

        public void register(final org.apache.geronimo.gshell.whisper.message.MessageHandler handler) {
            assert handler != null;

            Class<?> type = handler.getType();

            log.debug("Registering handler: {} for type: {}", handler, type);

            // noinspection unchecked
            addMessageHandler(type, handler);
        }

        @Override
        public void sessionOpened(final IoSession session) throws Exception {
            assert session != null;

            // Install the session context
            ClientSessionContext context = ClientSessionContext.BINDER.bind(session, new ClientSessionContext());
            log.debug("Created session context: {}", context);
        }

        @Override
        public void sessionClosed(final IoSession session) throws Exception {
            assert session != null;

            ClientSessionContext context = ClientSessionContext.BINDER.unbind(session);
            log.debug("Removed session context: {}", context);
        }
    }
}