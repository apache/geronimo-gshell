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

package org.apache.geronimo.gshell.remote.server.handler;

import jline.Completor;
import org.apache.geronimo.gshell.remote.message.CompleteMessage;
import org.apache.geronimo.gshell.remote.server.RemoteShellImpl;
import org.apache.geronimo.gshell.shell.Shell;
import org.apache.geronimo.gshell.shell.ShellContext;
import org.apache.geronimo.gshell.shell.ShellContextHolder;
import org.apache.geronimo.gshell.whisper.transport.Session;

import java.util.ArrayList;
import java.util.List;

/**
 * Server handler for {@link CompleteMessage} messages.
 *
 * @version $Rev: 706033 $ $Date: 2008-10-19 22:36:15 +0700 (Sun, 19 Oct 2008) $
 */
public class CompleteHandler
    extends ServerMessageHandlerSupport<CompleteMessage>
{
    public CompleteHandler() {
        super(CompleteMessage.class);
    }

    public void handle(final Session session, final ServerSessionContext context, final CompleteMessage message) throws Exception {
        assert session != null;
        assert context != null;
        assert message != null;

        ShellContext prevContext = ShellContextHolder.get(true);
        ShellContextHolder.set(context.shellContext);

        List<String> candidates = new ArrayList<String>();
        int position = -1;

        try {
            Shell shell = context.shellContext.getShell();
            if (shell instanceof RemoteShellImpl) {
                List<Completor> completors = ((RemoteShellImpl)shell).getCompleters();
                for (Completor completor : completors) {
                    if ((position = completor.complete(message.getBuffer(), message.getCursor(), candidates)) != -1) {
                        break;
                    }
                }
            }
        }
        finally {
            ShellContextHolder.set(prevContext);
        }

        CompleteMessage.Result reply = new CompleteMessage.Result(candidates, position);
        reply.setCorrelationId(message.getId());
        session.send(reply);
    }
}
