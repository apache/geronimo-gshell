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

import org.apache.geronimo.gshell.remote.client.handler.ClientMessageHandler;
import org.apache.geronimo.gshell.remote.client.handler.ClientSessionContext;
import org.apache.geronimo.gshell.security.crypto.CryptoContext;
import org.apache.geronimo.gshell.remote.message.CloseShellMessage;
import org.apache.geronimo.gshell.remote.message.ConnectMessage;
import org.apache.geronimo.gshell.remote.message.EchoMessage;
import org.apache.geronimo.gshell.remote.message.ExecuteMessage;
import org.apache.geronimo.gshell.remote.message.LoginMessage;
import org.apache.geronimo.gshell.remote.message.OpenShellMessage;
import org.apache.geronimo.gshell.whisper.message.Message;
import org.apache.geronimo.gshell.whisper.message.MessageHandler;
import org.apache.geronimo.gshell.whisper.transport.Session;
import org.apache.geronimo.gshell.whisper.transport.Transport;
import org.apache.geronimo.gshell.whisper.transport.TransportFactory;
import org.apache.geronimo.gshell.whisper.transport.TransportFactoryLocator;
import org.apache.mina.common.IoSession;
import org.apache.mina.handler.demux.DemuxingIoHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.security.auth.login.LoginException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

/**
 * Provides support for the client-side of the remote shell protocol.
 *
 * @version $Rev$ $Date$
 */
public class RshClient
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private CryptoContext crypto;

    @Autowired
    private TransportFactoryLocator locator;

    private List<ClientMessageHandler> handlers;

    private Transport transport;

    private Session session;

    public RshClient(final List<ClientMessageHandler> handlers) {
        assert handlers != null;

        this.handlers = handlers;
    }

    public void connect(final URI remote, final URI local) throws Exception {
        TransportFactory factory = locator.locate(remote);

        transport = factory.connect(remote, local, new Handler());
        session = transport.getSession();

        log.debug("Connected to: {}", remote);
    }

    public InputStream getInputStream() {
        return session.getInputStream();
    }

    public OutputStream getOutputStream() {
        return session.getOutputStream();
    }

    public Transport getTransport() {
        return transport;
    }

    public void close() {
        transport.close();
    }

    public void login(final String username, final String password) throws Exception {
        doHandshake();
        doLogin(username, password);
    }

    private void doHandshake() throws Exception {
        log.debug("Handshaking");

        ClientSessionContext context = ClientSessionContext.BINDER.lookup(session.getSession());

        Message response = session.request(new ConnectMessage(crypto.getPublicKey()));

        if (response instanceof ConnectMessage.Result) {
            ConnectMessage.Result result = (ConnectMessage.Result)response;
            context.pk = result.getPublicKey();
        }
        else {
            throw new InternalError("Unexpected handshake response: " + response);
        }
    }

    private void doLogin(final String username, final String password) throws Exception {
        log.debug("Logging in: {}", username);

        ClientSessionContext context = ClientSessionContext.BINDER.lookup(session.getSession());

        // Send the login message
        Message response;
        try {
            response = session.request(new LoginMessage(username, password));
        }
        catch (Exception e) {
            throw new LoginException(e.getMessage());
        }

        if (response instanceof LoginMessage.Success) {
            context.identity = ((LoginMessage.Success)response).getToken();

            log.debug("Client identity: {}", context.identity);
        }
        else if (response instanceof LoginMessage.Failure) {
            LoginMessage.Failure failure = (LoginMessage.Failure)response;

            // FIXME: Remove this jaas exception once we figure out how to use jsecurity in this context
            throw new LoginException("Login failed: " + failure.getReason());
        }
    }
    
    public void echo(final String text) throws Exception {
        assert text != null;
        
        log.debug("Echoing: {}", text);

        session.send(new EchoMessage(text)).join();
    }

    public void openShell() throws Exception {
        log.debug("Opening remote shell");

        Message resp = session.request(new OpenShellMessage());

        //
        // TODO: Need some context from the response
        //

        log.trace("Response: {}", resp);
    }

    public void closeShell() throws Exception {
        log.debug("Closing remote shell");

        Message resp = session.request(new CloseShellMessage());

        //
        // TODO: Need some context from the response
        //

        log.trace("Response: {}", resp);
    }

    private Object doExecute(final ExecuteMessage msg) throws Exception {
        assert msg != null;

        ExecuteMessage.Result result = (ExecuteMessage.Result) session.request(msg);

        // Handle result notifications
        if (result instanceof ExecuteMessage.NotificationResult) {
            ExecuteMessage.NotificationResult n = (ExecuteMessage.NotificationResult)result;

            throw n.getNotification();
        }

        // Handle result faults
        if (result instanceof ExecuteMessage.FailureResult) {
            ExecuteMessage.FailureResult failure = (ExecuteMessage.FailureResult)result;

            //noinspection ThrowableResultOfMethodCallIgnored
            throw new RemoteExecuteException(failure.getCause());
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

    public Object execute(final Object[][] cmds) throws Exception {
        assert cmds != null;

        return doExecute(new ExecuteMessage(cmds));
    }

    //
    // IO Handler
    //

    private class Handler
        extends DemuxingIoHandler
    {
        public Handler() throws Exception {
            // Complain if we don't have any handlers
            if (handlers.isEmpty()) {
                throw new Error("No message handlers were discovered");
            }

            for (ClientMessageHandler handler : handlers) {
                register(handler);
            }
        }

        public void register(final MessageHandler handler) {
            assert handler != null;

            Class<?> type = handler.getType();

            log.debug("Registering handler: {} -> {}", type.getSimpleName(), handler);

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